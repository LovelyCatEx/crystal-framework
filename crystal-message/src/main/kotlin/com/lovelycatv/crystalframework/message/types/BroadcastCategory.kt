package com.lovelycatv.crystalframework.message.types

/**
 * The business kind of a broadcast. The broadcast base table is generic
 * (write-once, read-fanout); this discriminator is what lets multiple broadcast
 * businesses share the same table — a new kind is a new value here, not a new table.
 *
 * Scope (SYSTEM / TENANT) already tells you who sends; [category] tells you what
 * business the broadcast belongs to.
 */
enum class BroadcastCategory(val typeId: Int) {
    /** A system-wide or tenant-wide announcement. */
    ANNOUNCEMENT(0);

    companion object {
        fun getByTypeId(typeId: Int): BroadcastCategory? = entries.find { it.typeId == typeId }
    }
}
