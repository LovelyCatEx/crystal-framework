package com.lovelycatv.crystalframework.tenant.constants

import com.lovelycatv.crystalframework.shared.utils.KotlinObjectClassUtils
import com.lovelycatv.crystalframework.tenant.types.TenantPermissionType

object TenantPermission {
    val MENU_TENANT_PROFILE_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.profile",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/profile"
    )

    const val ACTION_TENANT_PROFILE_READ_PEM = "i.tenant.profile.read"
    val ACTION_TENANT_PROFILE_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_PROFILE_READ_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_PROFILE_UPDATE_PEM = "i.tenant.profile.update"
    val ACTION_TENANT_PROFILE_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_PROFILE_UPDATE_PEM,
        type = TenantPermissionType.ACTION,
    )

    fun allPermissions(): List<TenantPermissionDeclaration> {
        return KotlinObjectClassUtils.extractAllValProperties(TenantPermission, false)
    }
}