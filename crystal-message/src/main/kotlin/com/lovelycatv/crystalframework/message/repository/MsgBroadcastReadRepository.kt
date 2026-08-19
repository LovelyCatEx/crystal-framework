package com.lovelycatv.crystalframework.message.repository

import com.lovelycatv.crystalframework.message.entity.MsgBroadcastReadEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface MsgBroadcastReadRepository : BaseRepository<MsgBroadcastReadEntity> {
    fun findAllByUserId(userId: Long): Flux<MsgBroadcastReadEntity>

    @Modifying
    @Query("""
        INSERT INTO msg_broadcast_reads (id, broadcast_id, user_id, read_time, created_time, modified_time)
        VALUES (:id, :broadcastId, :userId, :readTime, :createdTime, :modifiedTime)
        ON CONFLICT (broadcast_id, user_id) WHERE deleted_time IS NULL DO NOTHING
    """)
    fun insertIgnoringExisting(
        id: Long,
        broadcastId: Long,
        userId: Long,
        readTime: Long,
        createdTime: Long,
        modifiedTime: Long,
    ): Mono<Long>
}
