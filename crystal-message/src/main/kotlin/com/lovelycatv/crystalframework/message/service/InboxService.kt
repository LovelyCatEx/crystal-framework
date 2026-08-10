package com.lovelycatv.crystalframework.message.service

import com.lovelycatv.crystalframework.message.controller.vo.ConversationInboxVO

/**
 * A user's unified inbox view across both fanout strategies: write-diffusion
 * conversations and read-diffusion announcements.
 */
interface InboxService {
    /**
     * The user's conversations (write-diffusion reverse index), newest-active first, each
     * enriched with the counterpart party so the frontend can render titles directly.
     *
     * [currentTenantId] is the tenant the caller is currently acting as (null when acting as a plain
     * system user); it only decides each entry's `counterpartInCurrentOrg` flag and never filters the
     * result — the inbox always spans every scope.
     */
    suspend fun listConversations(userId: Long, currentTenantId: Long?): List<ConversationInboxVO>

    /**
     * Total unread badge = unread conversation messages + unread announcements.
     * [tenantIds] scopes announcement audience matching (caller-supplied).
     */
    suspend fun totalUnread(userId: Long, tenantIds: Collection<Long>): Long
}
