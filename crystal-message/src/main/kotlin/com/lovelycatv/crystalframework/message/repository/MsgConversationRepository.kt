package com.lovelycatv.crystalframework.message.repository

import com.lovelycatv.crystalframework.message.entity.MsgConversationEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface MsgConversationRepository : BaseRepository<MsgConversationEntity> {
    fun findByDedupeKey(dedupeKey: String): Mono<MsgConversationEntity>
}
