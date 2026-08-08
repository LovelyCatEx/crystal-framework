package com.lovelycatv.crystalframework.message.repository

import com.lovelycatv.crystalframework.message.entity.MsgConversationMemberEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface MsgConversationMemberRepository : BaseRepository<MsgConversationMemberEntity> {
    fun findAllByUserId(userId: Long): Flux<MsgConversationMemberEntity>

    fun findByConversationIdAndUserId(conversationId: Long, userId: Long): Mono<MsgConversationMemberEntity>

    fun findAllByConversationId(conversationId: Long): Flux<MsgConversationMemberEntity>
}
