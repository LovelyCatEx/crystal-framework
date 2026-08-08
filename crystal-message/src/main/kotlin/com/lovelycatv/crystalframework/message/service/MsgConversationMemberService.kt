package com.lovelycatv.crystalframework.message.service

import com.lovelycatv.crystalframework.message.entity.MsgConversationMemberEntity
import com.lovelycatv.crystalframework.message.repository.MsgConversationMemberRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface MsgConversationMemberService :
    CachedBaseService<MsgConversationMemberRepository, MsgConversationMemberEntity> {
    /** Return the user's membership row for a conversation, creating it (unread = 0) if absent. */
    suspend fun getOrCreate(conversationId: Long, userId: Long): MsgConversationMemberEntity

    /** Bump a member's unread counter by one (cache-safe update). No-op if the row is missing. */
    suspend fun incrementUnread(conversationId: Long, userId: Long): MsgConversationMemberEntity?

    /** Clear unread and advance the read cursor to [lastReadMessageId]. No-op if the row is missing. */
    suspend fun markRead(
        conversationId: Long,
        userId: Long,
        lastReadMessageId: Long?,
    ): MsgConversationMemberEntity?

    /** All conversation memberships for a user (the inbox reverse index). */
    suspend fun listByUser(userId: Long): List<MsgConversationMemberEntity>
}
