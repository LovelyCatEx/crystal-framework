package com.lovelycatv.crystalframework.tenant.types

enum class TenantMemberStatus(val typeId: Int) {
    INACTIVE(0),
    DEPARTED(1),
    RESIGNED(2),
    REVIEWING(3),
    ACTIVE(4);

    companion object {
        fun getByType(typeId: Int): TenantMemberStatus? {
            return entries.find { it.typeId == typeId }
        }
    }
}