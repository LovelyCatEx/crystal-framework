package com.lovelycatv.crystalframework.system.service.impl

import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.utils.withDistributedLock
import com.lovelycatv.crystalframework.system.constants.SystemInitializeConstants
import com.lovelycatv.crystalframework.system.service.SystemInitializeService
import com.lovelycatv.crystalframework.system.service.SystemInitializeTransactionService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

@Service
class SystemInitializeServiceImpl(
    private val reactiveRedisService: ReactiveRedisService,
    private val transactionService: SystemInitializeTransactionService,
) : SystemInitializeService {
    private val logger = logger()
    private val secureRandom = SecureRandom()

    override suspend fun prepareInitializationToken(): String? {
        return reactiveRedisService.withDistributedLock(
            lockKey = RedisConstants.LOCK_SYSTEM_INITIALIZE,
            ttl = SystemInitializeConstants.LOCK_TTL,
            busyMessage = SystemInitializeConstants.LOCK_BUSY_MESSAGE,
        ) {
            if (transactionService.isSystemInitialized()) {
                reactiveRedisService.removeKey(RedisConstants.SYSTEM_INITIALIZE_TOKEN).awaitFirstOrNull()
                return@withDistributedLock null
            }

            val candidate = generateToken()
            val created = reactiveRedisService
                .setIfAbsent(RedisConstants.SYSTEM_INITIALIZE_TOKEN, candidate)
                .awaitFirstOrNull() == true

            candidate.takeIf { created }
        }
    }

    override suspend fun isSystemInitialized(): Boolean {
        return transactionService.isSystemInitialized()
    }

    override suspend fun initializeSystem(
        initializationToken: String?,
        username: String,
        password: String,
        email: String,
        smtpHost: String,
        smtpPort: Int,
        smtpUsername: String,
        smtpPassword: String,
        fromEmail: String,
        fromName: String,
    ) {
        reactiveRedisService.withDistributedLock(
            lockKey = RedisConstants.LOCK_SYSTEM_INITIALIZE,
            ttl = SystemInitializeConstants.LOCK_TTL,
            busyMessage = SystemInitializeConstants.LOCK_BUSY_MESSAGE,
        ) {
            if (transactionService.isSystemInitialized()) {
                reactiveRedisService.removeKey(RedisConstants.SYSTEM_INITIALIZE_TOKEN).awaitFirstOrNull()
                throw BusinessException("System has already been initialized")
            }

            val storedToken = reactiveRedisService
                .get<String>(RedisConstants.SYSTEM_INITIALIZE_TOKEN)
                .awaitFirstOrNull()

            if (!matches(initializationToken, storedToken)) {
                throw ForbiddenException(
                    "Invalid system initialization token",
                    context = ForbiddenContext(
                        reason = ForbiddenReason.INVALID_INITIALIZATION_TOKEN,
                        scope = ResourceScope.SYSTEM,
                    ),
                )
            }

            transactionService.initializeSystem(
                username = username,
                password = password,
                email = email,
                smtpHost = smtpHost,
                smtpPort = smtpPort,
                smtpUsername = smtpUsername,
                smtpPassword = smtpPassword,
                fromEmail = fromEmail,
                fromName = fromName,
            )

            try {
                val consumed = reactiveRedisService
                    .compareAndDelete(RedisConstants.SYSTEM_INITIALIZE_TOKEN, storedToken!!)
                    .awaitFirstOrNull() == true

                if (!consumed) {
                    logger.error("System initialized, but the initialization token could not be consumed")
                }
            } catch (e: Exception) {
                logger.error("System initialized, but the initialization token cleanup failed", e)
            }
        }
    }

    private fun generateToken(): String {
        val bytes = ByteArray(SystemInitializeConstants.TOKEN_BYTES)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun matches(submittedToken: String?, storedToken: String?): Boolean {
        if (submittedToken.isNullOrBlank() || storedToken == null) {
            return false
        }

        return MessageDigest.isEqual(
            submittedToken.toByteArray(Charsets.UTF_8),
            storedToken.toByteArray(Charsets.UTF_8),
        )
    }
}
