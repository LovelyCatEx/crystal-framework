package com.lovelycatv.crystalframework.sdk.rbac.tenant.types

enum class TenantPermissionType(val typeId: Int) {
    ACTION(0),
    MENU(1);

    companion object {
        fun getById(id: Int): TenantPermissionType? {
            return entries.find { it.typeId == id }
        }
    }
}
