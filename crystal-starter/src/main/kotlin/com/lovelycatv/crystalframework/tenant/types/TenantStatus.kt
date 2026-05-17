package com.lovelycatv.crystalframework.tenant.types

enum class TenantStatus(val typeId: Int) {
    REVIEWING(0),
    ACTIVE(1),
    CLOSED(2);

    companion object {
        fun getTenantStatusType(typeId: Int): TenantStatus? {
            return entries.find { it.typeId == typeId }
        }
    }
}