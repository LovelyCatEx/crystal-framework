package com.lovelycatv.crystalframework.sdk.message.types

/**
 * How a party's read cursor is tracked inside a conversation.
 *
 * This single enum is the only difference between "shared customer-service inbox"
 * and "everyone has their own unread count" — not two separate code paths.
 */
enum class ReadGranularity(val typeId: Int) {
    /** Each real user keeps an independent read cursor / unread count. */
    PER_USER(0),

    /** All real users behind the party share a single read cursor (e.g. tenant CS desk). */
    SHARED(1);

    companion object {
        fun getByTypeId(typeId: Int): ReadGranularity? = entries.find { it.typeId == typeId }
    }
}
