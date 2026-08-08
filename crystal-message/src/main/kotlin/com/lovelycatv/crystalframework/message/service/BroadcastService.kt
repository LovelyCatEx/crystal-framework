package com.lovelycatv.crystalframework.message.service

import com.lovelycatv.crystalframework.message.entity.MsgBroadcastEntity

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
}
