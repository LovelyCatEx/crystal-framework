package com.lovelycatv.crystalframework.resource.types

enum class FileResourceStatus(val typeId: Int) {
    UPLOADING(0),
    COMMITTED(1),
    CLEANUP_PENDING(2);

    companion object {
        fun getByTypeId(typeId: Int): FileResourceStatus? {
            return entries.find { it.typeId == typeId }
        }
    }
}
