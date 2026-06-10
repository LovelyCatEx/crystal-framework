package com.lovelycatv.crystalframework.shared.types.tenant

enum class DictTypeStatus(val typeId: Int) {
    DISABLED(0),
    ENABLED(1);

    companion object {
        fun getById(id: Int): DictTypeStatus? {
            return entries.find { it.typeId == id }
        }
    }
}
