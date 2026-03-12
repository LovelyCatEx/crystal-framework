package com.lovelycatv.crystalframework.user.service.impl

import com.lovelycatv.crystalframework.mail.constants.SystemMailDeclaration
import com.lovelycatv.crystalframework.rbac.constants.SystemRole
import com.lovelycatv.crystalframework.rbac.service.UserRoleRelationService
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.FileResourceServiceManager
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.system.types.RedisConstants
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.tenant.types.TenantStatus
import com.lovelycatv.crystalframework.user.controller.dto.UpdateUserProfileDTO
import com.lovelycatv.crystalframework.user.controller.vo.UserProfileVO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import com.lovelycatv.crystalframework.user.service.EmailCodeAuthService
import com.lovelycatv.crystalframework.user.service.OAuthAccountService
import com.lovelycatv.crystalframework.user.service.UserRbacQueryService
import com.lovelycatv.crystalframework.user.service.UserService
import com.lovelycatv.crystalframework.user.service.result.UserRbacQueryResult
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Lazy
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import java.util.*
import kotlin.reflect.KClass

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val passwordEncoder: PasswordEncoder,
    private val userRoleRelationService: UserRoleRelationService,
    private val redisService: RedisService,
    private val fileResourceService: FileResourceService,
    private val fileResourceServiceManager: FileResourceServiceManager,
    override val eventPublisher: ApplicationEventPublisher,
    private val oAuthAccountService: OAuthAccountService,
    private val emailCodeAuthService: EmailCodeAuthService,
    private val userRbacQueryService: UserRbacQueryService,
    @Lazy
    private val tenantService: TenantService,
    private val tenantMemberRelationService: TenantMemberRelationService
) : UserService {
    private val logger = logger()

    override fun getRepository(): UserRepository {
        return this.userRepository
    }

    override fun findByUsername(username: String): Mono<UserDetails> {
        val (realUsername, tenantIdStr) = username.split(":")

        val tenantMono: Mono<TenantEntity> = with(tenantIdStr.toLong()) {
            mono {
                if (this@with > 0) {
                    val tenant = tenantService.getByIdOrThrow(
                        this@with,
                        BusinessException("tenant $tenantIdStr is not found")
                    )

                    if (tenant.getRealStatus() == TenantStatus.ACTIVE) {
                        tenant
                    } else {
                        throw BusinessException("tenant is inactive or closed")
                    }
                } else {
                    null
                }
            }
        }

        return this@UserServiceImpl
            .getRepository()
            .findByUsernameOrEmail(realUsername, realUsername)
            .switchIfEmpty {
                Mono.error(BusinessException("User $realUsername not found"))
            }
            .map { userEntity ->
                userEntity.apply {
                    setInternalRawAuthorities(
                        runBlocking(Dispatchers.IO) {
                            userRoleRelationService
                                .getUserRoles(userEntity.id)
                                .map { it.name }
                        }
                    )
                }
            }
            .flatMap { userEntity ->
                tenantMono
                    .flatMap<UserDetails> {
                        userEntity.setAuthenticatedTenant(it)
                        flux {
                            tenantMemberRelationService
                                .getUserTenantMembers(userEntity.id)
                                .forEach { this.send(it) }
                        }.collectList()
                            .flatMap {
                                if (!it.any { it.tenantId == tenantIdStr.toLong() && it.memberUserId == userEntity.id }) {
                                    Mono.error(BusinessException("User $realUsername not found in target tenant"))
                                } else {
                                    Mono.empty()
                                }
                            }
                    }
                    .switchIfEmpty {
                        userEntity.toMono()
                    }
            }
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun register(
        username: String,
        password: String,
        email: String,
        emailConfirmationCode: String
    ) {
        val existingUser = this
            .getRepository()
            .findByUsernameOrEmail(username, email)
            .awaitSingleOrNull()

        emailCodeAuthService.checkCachedEmailCode(
            RedisConstants.getRequestRegisterEmailCodeKey(email),
            emailConfirmationCode
        )

        if (existingUser != null) {
            if (existingUser.username == username) {
                throw BusinessException("User $username already exists")
            }

            if (existingUser.email == email) {
                throw BusinessException("Email $username already exists")
            }
        }

        val user = this.getRepository().save(
            UserEntity(
                id = snowIdGenerator.nextId(),
                username = username,
                password = encodePassword(password),
                email = email,
                nickname = username
            ) newEntity true
        ).awaitSingleOrNull() ?: throw BusinessException("User $username not registered")

        // Add role relations
        userRoleRelationService.setUserRolesByNames(user.id, listOf(SystemRole.ROLE_USER))

        logger.info("User ${user.username} / ${user.email} registered successfully, details: ${user.toJSONString()}")
    }

    override suspend fun requestRegisterEmailConfirmationCode(email: String) {
        emailCodeAuthService.withSendEmailCode(
            redisKey = RedisConstants.getRequestRegisterEmailCodeKey(email)
        ) { code, mailService ->
            mailService.sendMailByType(
                email,
                SystemMailDeclaration.systemUserRegisterTemplateType.name,
                mapOf(
                    SystemMailDeclaration.VARIABLE_EMAIL_CODE to code
                )
            )
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun resetPassword(email: String, emailCode: String, newPassword: String) {
        emailCodeAuthService.checkCachedEmailCode(
            RedisConstants.getRequestResetPasswordEmailCodeKey(email),
            emailCode
        )

        val existingUser = this
            .getRepository()
            .findByEmail(email)
            .awaitFirstOrNull()

        if (existingUser == null) {
            throw BusinessException("User $email not found")
        }

        withUpdateEntityContext(existingUser) {
            this.getRepository().save(
                existingUser.apply {
                    password = encodePassword(newPassword)
                }
            ).awaitFirstOrNull() ?: throw BusinessException("Could not reset password")
        }

        logger.info("Password of user ${existingUser.username} / ${existingUser.email} has been reset")
    }

    override suspend fun requestResetPasswordEmailConfirmationCode(email: String) {
        emailCodeAuthService.withSendEmailCode(
            redisKey = RedisConstants.getRequestResetPasswordEmailCodeKey(email)
        ) { code, mailService ->
            mailService.sendMail(
                email,
                SystemMailDeclaration.systemResetPasswordTemplateType.name,
                mapOf(
                    SystemMailDeclaration.VARIABLE_EMAIL_CODE to code
                )
            )
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun resetEmailAddress(userId: Long, emailCode: String, newEmail: String) {
        val user = this.getByIdOrThrow(userId)

        if (user.email == null) {
            throw BusinessException("email does not exist")
        }

        val oldEmail = user.email

        emailCodeAuthService.checkCachedEmailCode(
            RedisConstants.getRequestResetEmailAddressEmailCodeKey(newEmail),
            emailCode
        )

        withUpdateEntityContext(user) {
            this.getRepository()
                .save(user.apply { email = newEmail })
                .awaitFirstOrNull()
                ?: throw BusinessException("could not reset email address for ${user.id}")
        }

        logger.info("Email of user ${user.username} / ${user.email} has been reset from $oldEmail to $newEmail")
    }

    override suspend fun requestResetEmailAddressEmailConfirmationCode(email: String) {
        emailCodeAuthService.withSendEmailCode(
            redisKey = RedisConstants.getRequestResetEmailAddressEmailCodeKey(email)
        ) { code, mailService  ->
            mailService.sendMail(
                email,
                SystemMailDeclaration.systemResetEmailAddressTemplateType.name,
                mapOf(
                    SystemMailDeclaration.VARIABLE_EMAIL_CODE to code
                )
            )
        }
    }

    override suspend fun getUserRbacAccessInfo(userId: Long): UserRbacQueryResult {
        return userRbacQueryService.getUserRbacAccessInfo(userId)
    }

    override suspend fun getUserProfileVO(userId: Long, fullAccess: Boolean): UserProfileVO {
        val user = getByIdOrThrow(userId, BusinessException("User $userId not found"))

        return UserProfileVO(
            id = user.id,
            nickname = user.nickname,
            avatar = fileResourceService.getFileDownloadUrl(user.avatar),
            username = if (fullAccess) user.username else null,
            email = if (fullAccess) user.email else null,
            registeredTime = if (fullAccess) user.createdTime else null,
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun updateUserProfile(
        userId: Long,
        dto: UpdateUserProfileDTO
    ) {
        val user = getByIdOrThrow(userId, BusinessException("User $userId not found"))

        withUpdateEntityContext(userId) {
            this.getRepository().save(
                user.apply {
                    dto.nickname?.let {
                        nickname = it
                    }
                }
            ).awaitFirstOrNull() ?: throw BusinessException("could not update user profile")
        }

        logger.info("User ${user.username} / ${user.email} has been updated, details: ${dto.toJSONString()}")
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun uploadAvatar(userId: Long, file: FilePart) {
        val (_, extension) = file.filename().split(".")
        val targetFileName = UUID.randomUUID().toString() + "." + extension

        val service = fileResourceServiceManager
            .getService(userId, ResourceFileType.USER_AVATAR, targetFileName)

        val result = service.uploadFile(
            userId,
            ResourceFileType.USER_AVATAR,
            file,
            targetFileName
        )

        if (!result.success || result.fileResourceEntity == null) {
            logger.error("could not upload avatar for user: $userId, fileResource: ${result.fileResourceEntity.toJSONString()}", result.exception)
            throw BusinessException("could not upload avatar", result.exception)
        }

        withInvalidateEntityCacheContext(userId) {
            this.getRepository()
                .updateAvatar(userId, result.fileResourceEntity.id)
                .awaitFirstOrNull()
                ?: throw BusinessException("could not upload avatar for user: $userId, fileEntityId: ${result.fileResourceEntity.id}")
        }

        logger.info("User $userId uploaded avatar, resource details: ${result.fileResourceEntity.toJSONString()}")
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun bindUserFromOAuthAccount(
        oauthAccountId: Long,
        username: String,
        password: String
    ): UserEntity {
        val user = this.findByUsername(username).awaitFirstOrNull()
            ?: throw BusinessException("user $username not found")

        if (!passwordEncoder.matches(password, user.password)) {
            throw BusinessException("incorrect password")
        }

        oAuthAccountService.bindUser(oauthAccountId, (user as UserEntity).id)

        logger.info("User ${user.username} / ${user.email} bind a new OAuth account $oauthAccountId")

        return user
    }

    override suspend fun bindUserFromOAuthAccount(oauthAccountId: Long, userId: Long): UserEntity {
        oAuthAccountService.bindUser(oauthAccountId, userId)

        return this.getByIdOrThrow(userId)
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun registerFromOAuthAccount(
        oauthAccountId: Long,
        username: String,
        password: String,
        nickname: String
    ): UserEntity {
        val existingUser = this
            .getRepository()
            .findByUsername(username)
            .awaitSingleOrNull()

        if (existingUser != null) {
            if (existingUser.username == username) {
                throw BusinessException("User $username already exists")
            }
        }

        val user = this.getRepository().save(
            UserEntity(
                id = snowIdGenerator.nextId(),
                username = username,
                password = encodePassword(password),
                email = null,
                nickname = nickname
            ) newEntity true
        ).awaitSingleOrNull() ?: throw BusinessException("User $username not registered")

        // Add role relations
        userRoleRelationService.setUserRolesByNames(user.id, listOf(SystemRole.ROLE_USER))

        // Bind OAuth Account
        oAuthAccountService.bindUser(oauthAccountId, user.id)

        logger.info("User $username registered from OAuth account $oauthAccountId successfully, details: ${user.toJSONString()}")

        return user
    }

    private fun encodePassword(rawPassword: String): String {
        return passwordEncoder.encode(rawPassword)
            ?: throw BusinessException("Could not create user entity due to missing encoded password, encoding failed")
    }

    override val cacheStore: ExpiringKVStore<String, UserEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<UserEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<UserEntity> = UserEntity::class
}