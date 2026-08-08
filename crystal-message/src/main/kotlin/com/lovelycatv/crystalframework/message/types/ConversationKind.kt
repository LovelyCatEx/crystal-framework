package com.lovelycatv.crystalframework.message.types

/**
 * The structural shape of a conversation. Orthogonal to party/scope: a DIRECT
 * conversation has exactly two parties; a GROUP conversation is fronted by a
 * single group-like party (department / self-built group).
 */
enum class ConversationKind(val typeId: Int) {
    DIRECT(0),
    GROUP(1);

    companion object {
        fun getByTypeId(typeId: Int): ConversationKind? = entries.find { it.typeId == typeId }
    }
}
