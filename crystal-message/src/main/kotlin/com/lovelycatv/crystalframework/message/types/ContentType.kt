package com.lovelycatv.crystalframework.message.types

/**
 * Message body content type. Only [TEXT] is implemented for now; IMAGE / LINK
 * are reserved extension slots (not yet wired to crystal-resource).
 */
enum class ContentType(val typeId: Int) {
    TEXT(0),
    IMAGE(1),
    LINK(2);

    companion object {
        fun getByTypeId(typeId: Int): ContentType? = entries.find { it.typeId == typeId }
    }
}
