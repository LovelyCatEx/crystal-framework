package com.lovelycatv.crystalframework.message.repository

import com.lovelycatv.crystalframework.message.entity.MsgConversationPartyEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface MsgConversationPartyRepository : BaseRepository<MsgConversationPartyEntity> {
    fun findAllByConversationId(conversationId: Long): Flux<MsgConversationPartyEntity>
}
