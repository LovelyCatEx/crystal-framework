package com.lovelycatv.crystalframework.audit.service.impl

import com.lovelycatv.crystalframework.audit.service.SessionMonitorService
import com.lovelycatv.crystalframework.audit.types.SessionDescription
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.constants.SessionConstants
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Range
import org.springframework.session.data.redis.ReactiveRedisIndexedSessionRepository
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class SessionMonitorServiceImpl(
    private val reactiveRedisService: ReactiveRedisService,
    private val indexedSessionRepository: ReactiveRedisIndexedSessionRepository
) : SessionMonitorService {
    override suspend fun getSessionsCount(): Long {
        return reactiveRedisService
            .opsForZSet<Any>()
            .size(RedisConstants.SpringSession.EXPIRATIONS)
            .awaitFirstOrNull()
            ?: 0L
    }

    override suspend fun getSessions(
        page: Int,
        pageSize: Int,
        sessionId: String?,
    ): PaginatedResponseData<SessionDescription> {
        if (sessionId != null) {
            val session = buildSessionDescriptionBySessionId(sessionId)

            return if (session != null) {
                 PaginatedResponseData(
                    page = 1,
                    pageSize = pageSize,
                    records = listOf(session),
                    total = 1,
                    totalPages = 1
                )
            } else {
                PaginatedResponseData(
                    page = 1,
                    pageSize = pageSize,
                    records = listOf(),
                    total = 0,
                    totalPages = 0
                )
            }

        }
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

        @Suppress("UNCHECKED_CAST")
        val sessionKeys = indexedSessionRepository.sessionRedisOperations
            .opsForZSet()
            .reverseRange(
                RedisConstants.SpringSession.EXPIRATIONS,
                Range.closed(start, end),
            )
            .awaitListWithTimeout() as List<String>

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

    private suspend fun buildSessionDescriptionBySessionId(sessionId: String): SessionDescription? {
        val session = indexedSessionRepository.findById(sessionId).awaitSingleOrNull()
            ?: return null

        return SessionDescription(
            sessionId = sessionId,
            remoteIp = session.getAttribute(SessionConstants.AUDIT_REMOTE_IP),
            userAgent = session.getAttribute(SessionConstants.AUDIT_USER_AGENT),
            userId = session.getAttribute(SessionConstants.AUDIT_USER_ID),
            tenantId = session.getAttribute(SessionConstants.AUDIT_TENANT_ID),
        )
    }
}