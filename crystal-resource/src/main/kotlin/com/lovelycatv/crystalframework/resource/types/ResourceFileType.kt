package com.lovelycatv.crystalframework.resource.types

enum class ResourceFileType(val typeId: Int) {
    USER_AVATAR(0),
    TENANT_ICON(1),;

    companion object {
        fun getByTypeId(typeId: Int): ResourceFileType? {
            return entries.find { it.typeId == typeId }
        }
    }
}