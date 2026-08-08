package com.lovelycatv.crystalframework.message.repository

import com.lovelycatv.crystalframework.message.entity.MsgBroadcastReadEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface MsgBroadcastReadRepository : BaseRepository<MsgBroadcastReadEntity> {
    fun findByBroadcastIdAndUserId(broadcastId: Long, userId: Long): Mono<MsgBroadcastReadEntity>

    fun findAllByUserId(userId: Long): Flux<MsgBroadcastReadEntity>
}
