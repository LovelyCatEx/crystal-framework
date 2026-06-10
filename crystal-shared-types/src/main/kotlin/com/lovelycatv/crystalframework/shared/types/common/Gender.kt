package com.lovelycatv.crystalframework.shared.types.common

enum class Gender(val typeId: Int) {
    UNSPECIFIED(0),
    MALE(1),
    FEMALE(2),
    OTHER(3);

    companion object {
        fun getByType(typeId: Int): Gender? {
            return entries.find { it.typeId == typeId }
        }
    }
}
