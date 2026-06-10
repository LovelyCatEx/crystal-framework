package com.lovelycatv.crystalframework.shared.types.tenant

enum class DictItemStatus(val typeId: Int) {
    DISABLED(0),
    ENABLED(1);

    companion object {
        fun getById(id: Int): DictItemStatus? {
            return entries.find { it.typeId == id }
        }
    }
}
