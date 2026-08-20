package com.lovelycatv.crystalframework.message.repository

import com.lovelycatv.crystalframework.message.entity.MsgMessageEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface MsgMessageRepository : BaseRepository<MsgMessageEntity> {
    fun findAllByConversationId(conversationId: Long): Flux<MsgMessageEntity>
}
