package com.lovelycatv.crystalframework.system.service.impl

import com.lovelycatv.crystalframework.rbac.constants.SystemRole
import com.lovelycatv.crystalframework.rbac.service.UserRoleRelationService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.system.service.SystemInitializeService
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import com.lovelycatv.crystalframework.system.types.SystemSettingsConstants
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SystemInitializeServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val snowIdGenerator: SnowIdGenerator,
    private val userRoleRelationService: UserRoleRelationService,
    private val systemSettingsService: SystemSettingsService
) : SystemInitializeService {
    private val logger = logger()

    override suspend fun isSystemInitialized(): Boolean {
        return (userRepository.count().awaitFirstOrNull() ?: 0L) > 0
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun initializeSystem(
        username: String,
        password: String,
        email: String,
        smtpHost: String,
        smtpPort: Int,
        smtpUsername: String,
        smtpPassword: String,
        fromEmail: String,
        fromName: String
    ) {
        if (isSystemInitialized()) {
            throw BusinessException("System has already been initialized")
        }

        val existingUser = userRepository.findByUsernameOrEmail(username, email).awaitFirstOrNull()
        if (existingUser != null) {
            throw BusinessException("User $username or email $email already exists")
        }

        val userEntity = UserEntity(
            id = snowIdGenerator.nextId(),
            username = username,
            password = passwordEncoder.encode(password)!!,
            email = email,
            nickname = username
        ).apply { newEntity() }

        userRepository.save(userEntity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create root user")

        userRoleRelationService.setUserRolesByNames(userEntity.id, listOf(SystemRole.ROLE_ROOT, SystemRole.ROLE_ADMIN, SystemRole.ROLE_USER))

        systemSettingsService.setSettings(SystemSettingsConstants.Mail.SMTP.HOST, smtpHost)
        systemSettingsService.setSettings(SystemSettingsConstants.Mail.SMTP.PORT, smtpPort.toString())
        systemSettingsService.setSettings(SystemSettingsConstants.Mail.SMTP.USERNAME, smtpUsername)
        systemSettingsService.setSettings(SystemSettingsConstants.Mail.SMTP.PASSWORD, smtpPassword)
        systemSettingsService.setSettings(SystemSettingsConstants.Mail.SMTP.FROM_EMAIL, fromEmail)
        systemSettingsService.setSettings(SystemSettingsConstants.Mail.SMTP.SSL, false.toString())

        logger.info("System initialized successfully with root user: $username, email: $email")
    }
}