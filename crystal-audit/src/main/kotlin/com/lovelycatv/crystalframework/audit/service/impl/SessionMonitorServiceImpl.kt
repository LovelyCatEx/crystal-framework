package com.lovelycatv.crystalframework.audit.service.impl

import com.lovelycatv.crystalframework.audit.service.SessionMonitorService
import com.lovelycatv.crystalframework.audit.types.SessionDescription
import com.lovelycatv.crystalframework.audit.types.SessionType
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
        type: Int?,
    ): PaginatedResponseData<SessionDescription> {
        if (sessionId != null) {
            val session = buildSessionDescriptionBySessionId(sessionId)
            val matches = session != null && (type == null || session.type == type)

            return if (matches) {
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

        val totalRaw = getSessionsCount()

        if (totalRaw == 0L) {
            return PaginatedResponseData(
                page = page,
                pageSize = pageSize,
                records = emptyList(),
                total = 0L,
                totalPages = 0
            )
        }

        // Fast path: no type filter — slice on Redis and resolve only the current page.
        if (type == null) {
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

            val sessions = sessionKeys.mapNotNull { key -> buildSessionDescriptionBySessionId(key) }

            return PaginatedResponseData(
                page = page,
                pageSize = pageSize,
                records = sessions,
                total = totalRaw,
                totalPages = ceil(totalRaw.toDouble() / pageSize).toInt()
            )
        }

        // Slow path: type filter requires resolving every session to know its type,
        // then paginating in memory. Session cardinality is expected to stay low so
        // this is acceptable for a monitoring view.
        @Suppress("UNCHECKED_CAST")
        val allKeys = indexedSessionRepository.sessionRedisOperations
            .opsForZSet()
            .reverseRange(
                RedisConstants.SpringSession.EXPIRATIONS,
                Range.closed(0L, Long.MAX_VALUE),
            )
            .awaitListWithTimeout() as List<String>

        val filtered = allKeys
            .mapNotNull { key -> buildSessionDescriptionBySessionId(key) }
            .filter { it.type == type }

        val total = filtered.size.toLong()
        val startIdx = ((page - 1) * pageSize).coerceAtLeast(0)
        val paged = filtered.drop(startIdx).take(pageSize)

        return PaginatedResponseData(
            page = page,
            pageSize = pageSize,
            records = paged,
            total = total,
            totalPages = if (total == 0L) 0 else ceil(total.toDouble() / pageSize).toInt()
        )
    }

    private suspend fun buildSessionDescriptionBySessionId(sessionId: String): SessionDescription? {
        val session = indexedSessionRepository.findById(sessionId).awaitSingleOrNull()
            ?: return null

        val userId: Long? = session.getAttribute(SessionConstants.AUDIT_USER_ID)
        val tenantId: Long? = session.getAttribute(SessionConstants.AUDIT_TENANT_ID)

        val resolvedType = if (userId != null) SessionType.USER else SessionType.PROMETHEUS

        return SessionDescription(
            sessionId = sessionId,
            remoteIp = session.getAttribute(SessionConstants.AUDIT_REMOTE_IP),
            userAgent = session.getAttribute(SessionConstants.AUDIT_USER_AGENT),
            userId = userId,
            tenantId = tenantId,
            type = resolvedType.typeId,
        )
    }
}