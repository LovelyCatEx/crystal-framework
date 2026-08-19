package com.lovelycatv.crystalframework.message.repository

import com.lovelycatv.crystalframework.message.entity.MsgConversationEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface MsgConversationRepository : BaseRepository<MsgConversationEntity> {
    fun findByDedupeKey(dedupeKey: String): Mono<MsgConversationEntity>

    @Modifying
    @Query("""
        UPDATE msg_conversations
        SET last_message_id = :messageId, last_message_time = :messageTime, modified_time = :modifiedTime
        WHERE id = :conversationId AND deleted_time IS NULL
          AND (last_message_time IS NULL OR last_message_time < :messageTime
            OR (last_message_time = :messageTime AND last_message_id < :messageId))
    """)
    fun advanceLastMessage(
        conversationId: Long,
        messageId: Long,
        messageTime: Long,
        modifiedTime: Long,
    ): Mono<Long>
}
