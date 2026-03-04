package com.lovelycatv.crystalframework.user.service.impl

import com.lovelycatv.crystalframework.rbac.service.UserRolePermissionRelationService
import com.lovelycatv.crystalframework.rbac.service.UserRoleRelationService
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.FileResourceServiceManager
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.mail.MailService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.system.types.RedisConstants
import com.lovelycatv.crystalframework.user.controller.dto.UpdateUserProfileDTO
import com.lovelycatv.crystalframework.user.controller.vo.UserProfileVO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import com.lovelycatv.crystalframework.user.service.UserService
import com.lovelycatv.crystalframework.user.service.result.UserRbacQueryResult
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Duration
import java.util.*
import kotlin.reflect.KClass

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val passwordEncoder: PasswordEncoder,
    private val userRoleRelationService: UserRoleRelationService,
    private val rolePermissionRelationService: UserRolePermissionRelationService,
    private val mailService: MailService,
    private val redisService: RedisService,
    private val fileResourceService: FileResourceService,
    private val fileResourceServiceManager: FileResourceServiceManager,
    override val eventPublisher: ApplicationEventPublisher,
) : UserService {
    private val logger = logger()

    override fun getRepository(): UserRepository {
        return this.userRepository
    }

    override fun findByUsername(username: String): Mono<UserDetails> {
        return this
            .getRepository()
            .findByUsernameOrEmail(username, username)
            .switchIfEmpty {
                Mono.error(BusinessException("User $username not found"))
            }
            .map {
                it.apply {
                    setInternalRawAuthorities(
                        runBlocking(Dispatchers.IO) {
                            userRoleRelationService
                                .getUserRoles(it.id)
                                .map { it.name }
                        }
                    )
                }
            }
    }

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

        this.checkCachedEmailCode(
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

        this.getRepository().save(
            UserEntity(
                id = snowIdGenerator.nextId(),
                username = username,
                password = passwordEncoder.encode(password)
                    ?: throw BusinessException("Could not create user entity due to missing encoded password, encoding failed"),
                email = email,
                nickname = username
            ) newEntity true
        ).awaitSingleOrNull()
    }

    override suspend fun requestRegisterEmailConfirmationCode(email: String) {
        withSendEmailCode(
            redisKey = RedisConstants.getRequestRegisterEmailCodeKey(email)
        ) { code ->
            mailService.sendMail(email, "Register", code)
        }
    }

    override suspend fun resetPassword(email: String, emailCode: String, newPassword: String) {
        this.checkCachedEmailCode(
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

        this.getRepository().save(
            existingUser.apply {
                password = passwordEncoder.encode(newPassword)!!
            }
        ).awaitFirstOrNull()
    }

    override suspend fun requestResetPasswordEmailConfirmationCode(email: String) {
        withSendEmailCode(
            redisKey = RedisConstants.getRequestResetPasswordEmailCodeKey(email)
        ) { code ->
            mailService.sendMail(email, "Reset Email", code)
        }
    }

    override suspend fun resetEmailAddress(userId: Long, emailCode: String, newEmail: String) {
        val user = this.getByIdOrThrow(userId)

        this.checkCachedEmailCode(
            RedisConstants.getRequestResetPasswordEmailCodeKey(user.email),
            emailCode
        )

        this.getRepository()
            .save(user.apply { email = newEmail })
            .awaitFirstOrNull()
            ?: throw BusinessException("could not reset email address for ${user.id}")
    }

    override suspend fun requestResetEmailAddressEmailConfirmationCode(email: String) {
        withSendEmailCode(
            redisKey = RedisConstants.getRequestResetEmailAddressEmailCodeKey(email)
        ) { code ->
            mailService.sendMail(email, "Reset Email", code)
        }
    }

    override suspend fun getUserRbacAccessInfo(userId: Long): UserRbacQueryResult {
        return UserRbacQueryResult(
            userId = userId,
            rolesWithPermissions = userRoleRelationService.getUserRoles(userId).map {
                UserRbacQueryResult.UserRoleWithPermissions(
                    role = it,
                    permissions = rolePermissionRelationService.getRolePermissions(it.id)
                )
            }
        )
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

    override suspend fun updateUserProfile(
        userId: Long,
        dto: UpdateUserProfileDTO
    ) {
        val user = getByIdOrThrow(userId, BusinessException("User $userId not found"))

        this.getRepository().save(
            user.apply {
                dto.nickname?.let {
                    nickname = it
                }
            }
        ).awaitFirstOrNull() ?: throw BusinessException("could not update user profile")
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

        this.getRepository()
            .updateAvatar(userId, result.fileResourceEntity.id)
            .awaitFirstOrNull()
            ?: throw BusinessException("could not upload avatar for user: $userId, fileEntityId: ${result.fileResourceEntity.id}")
    }

    private suspend fun checkCachedEmailCode(
        redisKey: String,
        emailCode: String
    ) {
        val existingCode = redisService
            .get<String>(redisKey)
            .awaitFirstOrNull()

        if (existingCode == null) {
            throw BusinessException("invalid email code")
        }

        val (_, correctCode) = existingCode.split(":")

        if (emailCode != correctCode) {
            throw BusinessException("incorrect email code")
        }
    }

    private suspend fun withSendEmailCode(
        redisKey: String,
        validMinutes: Long = 5,
        action: suspend (code: String) -> Unit
    ) {
        val existingCode = redisService
            .get<String>(redisKey)
            .awaitFirstOrNull()

        if (existingCode != null) {
            val codeCreatedTime = existingCode.split(":")[0].toLong()
            if (System.currentTimeMillis() - codeCreatedTime <= 60 * 1000L) {
                throw BusinessException("request email code frequently")
            }
        }

        val code = (100000..999999).random().toString()

        redisService
            .set(redisKey, "${System.currentTimeMillis()}:$code", Duration.ofMinutes(validMinutes))
            .awaitFirstOrNull()

        action.invoke(code)
    }

    override val cacheStore: ExpiringKVStore<String, UserEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<UserEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<UserEntity> = UserEntity::class
}