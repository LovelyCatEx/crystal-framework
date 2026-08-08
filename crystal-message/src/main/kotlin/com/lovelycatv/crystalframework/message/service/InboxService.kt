package com.lovelycatv.crystalframework.message.service

import com.lovelycatv.crystalframework.message.entity.MsgConversationMemberEntity

/**
 * A user's unified inbox view across both fanout strategies: write-diffusion
 * conversations and read-diffusion announcements.
 */
interface InboxService {
    /** The user's conversation memberships (write-diffusion reverse index). */
    suspend fun listConversations(userId: Long): List<MsgConversationMemberEntity>

    /**
     * Total unread badge = unread conversation messages + unread announcements.
     * [tenantIds] scopes announcement audience matching (caller-supplied).
     */
    suspend fun totalUnread(userId: Long, tenantIds: Collection<Long>): Long
}
