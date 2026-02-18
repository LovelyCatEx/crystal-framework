package com.lovelycatv.template.springboot.user.service

import com.lovelycatv.template.springboot.rbac.service.UserRolePermissionRelationService
import com.lovelycatv.template.springboot.rbac.service.UserRoleRelationService
import com.lovelycatv.template.springboot.rbac.types.PermissionType
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.service.mail.MailService
import com.lovelycatv.template.springboot.shared.service.redis.RedisService
import com.lovelycatv.template.springboot.shared.utils.SnowIdGenerator
import com.lovelycatv.template.springboot.system.types.RedisConstants
import com.lovelycatv.template.springboot.user.controller.vo.UserProfileVO
import com.lovelycatv.template.springboot.user.entity.UserEntity
import com.lovelycatv.template.springboot.user.repository.UserRepository
import com.lovelycatv.template.springboot.user.service.result.UserRbacQueryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.Duration

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val passwordEncoder: PasswordEncoder,
    private val userRoleRelationService: UserRoleRelationService,
    private val rolePermissionRelationService: UserRolePermissionRelationService,
    private val mailService: MailService,
    private val redisService: RedisService
) : UserService {
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

        val emailCodeRedisKey = RedisConstants.getRequestRegisterEmailCodeKey(email)
        val existingCode = redisService
            .get<String>(emailCodeRedisKey)
            .awaitFirstOrNull()

        if (existingCode == null) {
            throw BusinessException("invalid email code")
        }

        val (_, correctCode) = existingCode.split(":")

        if (emailConfirmationCode != correctCode) {
            throw BusinessException("incorrect email code")
        }

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
        val redisKey = RedisConstants.getRequestRegisterEmailCodeKey(email)
        val existingCode = redisService
            .get<String>(redisKey)
            .awaitFirstOrNull()

        if (existingCode != null) {
            val codeCreatedTime = existingCode.split(":")[0].toLong()
            if (System.currentTimeMillis() - codeCreatedTime <= 60 * 1000L) {
                throw BusinessException("request register email frequently")
            }
        }

        val code = (100000..999999).random().toString()

        redisService
            .set(redisKey, "${System.currentTimeMillis()}:$code", Duration.ofMinutes(5))
            .awaitFirstOrNull()

        mailService.sendMail(email, "Register", code)
    }

    override suspend fun resetPassword(email: String, emailCode: String, newPassword: String) {
        val emailCodeRedisKey = RedisConstants.getRequestResetPasswordEmailCodeKey(email)
        val existingCode = redisService
            .get<String>(emailCodeRedisKey)
            .awaitFirstOrNull()

        if (existingCode == null) {
            throw BusinessException("invalid email code")
        }

        val (_, correctCode) = existingCode.split(":")

        if (emailCode != correctCode) {
            throw BusinessException("incorrect email code")
        }

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
        val redisKey = RedisConstants.getRequestResetPasswordEmailCodeKey(email)
        val existingCode = redisService
            .get<String>(redisKey)
            .awaitFirstOrNull()

        if (existingCode != null) {
            val codeCreatedTime = existingCode.split(":")[0].toLong()
            if (System.currentTimeMillis() - codeCreatedTime <= 60 * 1000L) {
                throw BusinessException("request reset password email frequently")
            }
        }

        val code = (100000..999999).random().toString()

        redisService
            .set(redisKey, "${System.currentTimeMillis()}:$code", Duration.ofMinutes(5))
            .awaitFirstOrNull()

        mailService.sendMail(email, "Reset Password", code)
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
            username = if (fullAccess) user.username else null,
            email = if (fullAccess) user.email else null,
            registeredTime = if (fullAccess) user.createdTime else null,
        )
    }
}