package com.lovelycatv.crystalframework.message.service.impl

import com.lovelycatv.crystalframework.message.entity.MsgBroadcastEntity
import com.lovelycatv.crystalframework.message.entity.MsgBroadcastReadEntity
import com.lovelycatv.crystalframework.message.repository.MsgBroadcastReadRepository
import com.lovelycatv.crystalframework.message.repository.MsgBroadcastRepository
import com.lovelycatv.crystalframework.message.service.BroadcastService
import com.lovelycatv.crystalframework.message.types.BroadcastInboxItem
import com.lovelycatv.crystalframework.sdk.message.Audience
import com.lovelycatv.crystalframework.sdk.message.AudienceCandidate
import com.lovelycatv.crystalframework.sdk.message.AudienceResolverRegistry
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class BroadcastServiceImpl(
    private val msgBroadcastRepository: MsgBroadcastRepository,
    private val msgBroadcastReadRepository: MsgBroadcastReadRepository,
    private val audienceResolverRegistry: AudienceResolverRegistry,
    private val snowIdGenerator: SnowIdGenerator,
) : BroadcastService {

    override suspend fun listUnread(userId: Long, tenantIds: Collection<Long>): List<MsgBroadcastEntity> {
        val now = System.currentTimeMillis()
        val readIds = readBroadcastIdsOf(userId)
        return resolveVisible(now, AudienceCandidate(userId = userId, tenantIds = tenantIds))
            .filter { it.id !in readIds }
            .filter { it.expireTime == null || it.expireTime!! > now }
    }

    override suspend fun unreadCount(userId: Long, tenantIds: Collection<Long>): Long =
        listUnread(userId, tenantIds).size.toLong()

    override suspend fun listHistory(
        userId: Long,
        tenantIds: Collection<Long>,
        page: Int,
        pageSize: Int,
    ): PaginatedResponseData<BroadcastInboxItem> {
        val now = System.currentTimeMillis()
        val readIds = readBroadcastIdsOf(userId)
        // History keeps read and expired broadcasts too — it only shares the "published + audience
        // matched" baseline with listUnread, not its expire/read filters. Newest first.
        val visible = resolveVisible(now, AudienceCandidate(userId = userId, tenantIds = tenantIds))
            .sortedByDescending { it.publishTime }
        val total = visible.size.toLong()
        val fromIndex = ((page - 1) * pageSize).coerceAtLeast(0)
        val pageRecords = if (fromIndex >= visible.size) emptyList()
        else visible.subList(fromIndex, minOf(fromIndex + pageSize, visible.size))
        val records = pageRecords.map { BroadcastInboxItem(it, it.id in readIds) }
        val totalPages = if (pageSize <= 0) 0 else ((total + pageSize - 1) / pageSize).toInt()
        return PaginatedResponseData(page, pageSize, total, totalPages, records)
    }

    /** Published (as of [now]) broadcasts whose audience the [candidate] matches. In-memory: audience
     *  resolution cannot be pushed to SQL. Soft-deleted rows are filtered by the SQL interceptor. */
    private suspend fun resolveVisible(now: Long, candidate: AudienceCandidate): List<MsgBroadcastEntity> =
        msgBroadcastRepository.findAllByPublishTimeLessThanEqual(now)
            .collectList().awaitFirstOrNull().orEmpty()
            .filter { matchesAudience(it, candidate) }

    private suspend fun readBroadcastIdsOf(userId: Long): Set<Long> =
        msgBroadcastReadRepository.findAllByUserId(userId)
            .collectList().awaitFirstOrNull().orEmpty()
            .map { it.broadcastId }.toSet()

    override suspend fun markRead(broadcastId: Long, userId: Long) {
        if (msgBroadcastReadRepository.findByBroadcastIdAndUserId(broadcastId, userId)
                .awaitFirstOrNull() != null
        ) return
        msgBroadcastReadRepository.save(
            MsgBroadcastReadEntity(
                id = snowIdGenerator.nextId(),
                broadcastId = broadcastId,
                userId = userId,
            ).apply { newEntity() }
        ).awaitFirstOrNull()
    }

    private suspend fun matchesAudience(broadcast: MsgBroadcastEntity, candidate: AudienceCandidate): Boolean {
        val audienceType = broadcast.getRealAudienceType()
        val audience = Audience(type = audienceType, ref = broadcast.audienceRef)
        return audienceResolverRegistry.resolve(audienceType).matches(audience, candidate)
    }
}
