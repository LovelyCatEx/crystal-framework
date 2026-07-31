package com.lovelycatv.crystalframework.resource.types

enum class RuleDistributionType(val typeId: Int) {
    FIRST_AVAILABLE(0),
    RANDOM(1);

    companion object {
        fun getByTypeId(typeId: Int): RuleDistributionType? {
            return entries.find { it.typeId == typeId }
        }
    }
}
