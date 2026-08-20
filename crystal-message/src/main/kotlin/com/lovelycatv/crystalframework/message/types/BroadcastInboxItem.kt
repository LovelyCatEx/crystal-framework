package com.lovelycatv.crystalframework.message.types

import com.lovelycatv.crystalframework.message.entity.MsgBroadcastEntity

/**
 * A broadcast paired with whether the querying user has already read it. Service-layer view used by
 * the history (all-visible) listing, where read and unread items coexist; the unread listing needs
 * no such pairing since every item there is unread by definition.
 */
data class BroadcastInboxItem(
    val broadcast: MsgBroadcastEntity,
    val read: Boolean,
)
