package com.lovelycatv.crystalframework.message.service

import com.lovelycatv.crystalframework.message.entity.MsgBroadcastEntity
import com.lovelycatv.crystalframework.message.types.BroadcastInboxItem
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData

/**
 * Read-fanout (read-diffusion) side of broadcasts. Broadcasts are written once
 * (by the manager / system layer); this service resolves which of them a given user
 * has not yet read, and records read markers lazily on first open.
 */
interface BroadcastService {
    /**
     * Broadcasts visible to [userId] — whose audience the user matches, that are
     * currently within their publish window — and not yet read. [tenantIds] is the set
     * of tenants the user belongs to (supplied by the caller so the core stays tenant-agnostic).
     */
    suspend fun listUnread(userId: Long, tenantIds: Collection<Long>): List<MsgBroadcastEntity>

    /** Count of [listUnread]. */
    suspend fun unreadCount(userId: Long, tenantIds: Collection<Long>): Long

    /** Record that [userId] has read [broadcastId]. Idempotent. */
    suspend fun markRead(broadcastId: Long, userId: Long)

    /**
     * Paginated history of every broadcast visible to [userId] — whose audience the user matches and
     * that is already published — including read and expired ones (newest first). This is the "see
     * all past announcements" view; unlike [listUnread] it deliberately keeps expired and
     * already-read broadcasts, tagging each with its read state. [tenantIds] is the user's tenant set.
     */
    suspend fun listHistory(
        userId: Long,
        tenantIds: Collection<Long>,
        page: Int,
        pageSize: Int,
    ): PaginatedResponseData<BroadcastInboxItem>
}
