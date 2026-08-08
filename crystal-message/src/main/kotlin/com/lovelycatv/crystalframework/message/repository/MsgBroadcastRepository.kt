package com.lovelycatv.crystalframework.message.repository

import com.lovelycatv.crystalframework.message.entity.MsgBroadcastEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface MsgBroadcastRepository : BaseRepository<MsgBroadcastEntity> {
    /** Broadcasts already published as of [publishTime] (soft-deleted rows are filtered by the SQL interceptor). */
    fun findAllByPublishTimeLessThanEqual(publishTime: Long): Flux<MsgBroadcastEntity>
}
