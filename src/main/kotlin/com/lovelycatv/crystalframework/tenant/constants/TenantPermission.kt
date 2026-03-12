package com.lovelycatv.crystalframework.tenant.constants

import com.lovelycatv.crystalframework.shared.utils.KotlinObjectClassUtils
import com.lovelycatv.crystalframework.tenant.types.TenantPermissionType

object TenantPermission {
    val MENU_TENANT_PROFILE_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.profile",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/profile"
    )

    val ACTION_TENANT_PROFILE_READ = TenantPermissionDeclaration(
        name = "i.tenant.profile.read",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_TENANT_PROFILE_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.profile.update",
        type = TenantPermissionType.ACTION,
    )

    fun allPermissions(): List<TenantPermissionDeclaration> {
        return KotlinObjectClassUtils.extractAllValProperties(TenantPermission, false)
    }
}