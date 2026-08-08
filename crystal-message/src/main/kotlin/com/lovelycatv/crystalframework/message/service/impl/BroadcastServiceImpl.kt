package com.lovelycatv.crystalframework.message.service.impl

import com.lovelycatv.crystalframework.message.entity.MsgBroadcastEntity
import com.lovelycatv.crystalframework.message.entity.MsgBroadcastReadEntity
import com.lovelycatv.crystalframework.message.repository.MsgBroadcastReadRepository
import com.lovelycatv.crystalframework.message.repository.MsgBroadcastRepository
import com.lovelycatv.crystalframework.message.service.BroadcastService
import com.lovelycatv.crystalframework.sdk.message.Audience
import com.lovelycatv.crystalframework.sdk.message.AudienceCandidate
import com.lovelycatv.crystalframework.sdk.message.AudienceResolverRegistry
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
        val readIds = msgBroadcastReadRepository.findAllByUserId(userId)
            .collectList().awaitFirstOrNull().orEmpty()
            .map { it.broadcastId }.toSet()
        val candidate = AudienceCandidate(userId = userId, tenantIds = tenantIds)
        return msgBroadcastRepository.findAllByPublishTimeLessThanEqual(now)
            .collectList().awaitFirstOrNull().orEmpty()
            .filter { it.id !in readIds }
            .filter { it.expireTime == null || it.expireTime!! > now }
            .filter { matchesAudience(it, candidate) }
    }

    override suspend fun unreadCount(userId: Long, tenantIds: Collection<Long>): Long =
        listUnread(userId, tenantIds).size.toLong()

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
