package com.lovelycatv.crystalframework.audit.service.impl

import com.lovelycatv.crystalframework.audit.service.SessionMonitorService
import com.lovelycatv.crystalframework.audit.types.SessionDescription
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class SessionMonitorServiceImpl(private val redisService: RedisService) : SessionMonitorService {
    override suspend fun getSessionsCount(): Long {
        return redisService
            .opsForZSet<Any>()
            .zCard(RedisConstants.SpringSession.EXPIRATIONS)
            ?: 0L
    }

    override suspend fun getSessions(
        page: Int,
        pageSize: Int
    ): PaginatedResponseData<SessionDescription> {
        val total = getSessionsCount()

        if (total == 0L) {
            return PaginatedResponseData(
                page = page,
                pageSize = pageSize,
                records = emptyList(),
                total = 0L,
                totalPages = 0
            )
        }

        val start = ((page - 1) * pageSize).toLong()
        val end = start + pageSize - 1

        val sessionKeys = redisService
            .opsForZSet<String>()
            .reverseRange(RedisConstants.SpringSession.EXPIRATIONS, start, end) ?: emptySet()

        val sessions = sessionKeys.mapNotNull { sessionId ->
            buildSessionDescriptionBySessionId(sessionId)
        }

        return PaginatedResponseData(
            page = page,
            pageSize = pageSize,
            records = sessions,
            total = total,
            totalPages = ceil(total.toDouble() / pageSize).toInt()
        )
    }

    private fun buildSessionDescriptionBySessionId(sessionId: String): SessionDescription {
        return SessionDescription(
            sessionId = sessionId,
            remoteIp = "",
            userAgent = "",
            userId = 0L,
            tenantId = 0L,
        )
    }
}