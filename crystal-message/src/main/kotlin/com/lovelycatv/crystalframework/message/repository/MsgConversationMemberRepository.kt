package com.lovelycatv.crystalframework.message.repository

import com.lovelycatv.crystalframework.message.entity.MsgConversationMemberEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface MsgConversationMemberRepository : BaseRepository<MsgConversationMemberEntity> {
    fun findAllByUserId(userId: Long): Flux<MsgConversationMemberEntity>

    fun findByConversationIdAndUserId(conversationId: Long, userId: Long): Mono<MsgConversationMemberEntity>

    fun findAllByConversationId(conversationId: Long): Flux<MsgConversationMemberEntity>

    @Modifying
    @Query("""
        UPDATE msg_conversation_members
        SET unread_count = unread_count + 1, modified_time = :modifiedTime
        WHERE conversation_id = :conversationId AND user_id = :userId AND deleted_time IS NULL
    """)
    fun incrementUnread(conversationId: Long, userId: Long, modifiedTime: Long): Mono<Long>
}
