package com.lovelycatv.crystalframework.auth.service.impl

import com.lovelycatv.crystalframework.auth.entity.UserLoginLogEntity
import com.lovelycatv.crystalframework.auth.event.UserLoginEvent
import com.lovelycatv.crystalframework.auth.repository.UserLoginLogRepository
import com.lovelycatv.crystalframework.auth.service.UserLoginLogService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class UserLoginLogServiceImpl(
    private val userLoginLogRepository: UserLoginLogRepository,
    private val snowIdGenerator: SnowIdGenerator
) : UserLoginLogService {

    override suspend fun recordLoginLog(event: UserLoginEvent) {
        val entity = UserLoginLogEntity(
            id = snowIdGenerator.nextId(),
            userId = event.userId,
            username = event.username,
            tenantId = event.tenantId,
            loginMethod = event.loginMethod,
            oauth2Type = event.oauth2Type,
            oauth2Username = event.oauth2Username,
            oauth2AccountId = event.oauth2AccountId,
            success = event.success,
            errorMessage = event.errorMessage,
            remoteIp = event.remoteIp,
            userAgent = event.userAgent
        ).apply { newEntity() }

        userLoginLogRepository.save(entity).awaitFirstOrNull()
    }
}