package com.lovelycatv.crystalframework.resource.constants

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix

/**
 * Single source of truth for the file-resource manager-line authorisation matrix.
 *
 * Declared once here and shared by both [ManagerFileResourceController]
 * (com.lovelycatv.crystalframework.resource.controller.manager.file) — which uses it for the
 * standard CRUD authorisation — and the resource access service, which reuses it to decide whether
 * a viewer holding a manage authority may bypass per-resource visibility (OWNER_ONLY / SCOPE_MEMBER)
 * on the public download path. Keeping one instance avoids maintaining the 16 authority strings in
 * two places.
 */
object FileResourcePermissions {
    val MATRIX = PermissionMatrix(
        superCreate = SystemPermission.ACTION_X_FILE_RESOURCE_CREATE.name,
        superRead = SystemPermission.ACTION_X_FILE_RESOURCE_READ.name,
        superUpdate = SystemPermission.ACTION_X_FILE_RESOURCE_UPDATE.name,
        superDelete = SystemPermission.ACTION_X_FILE_RESOURCE_DELETE.name,
        systemCreate = SystemPermission.ACTION_SYSTEM_FILE_RESOURCE_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_FILE_RESOURCE_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_FILE_RESOURCE_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_FILE_RESOURCE_DELETE.name,
        tenantAdminCreate = SystemPermission.ACTION_TENANT_FILE_RESOURCE_CREATE.name,
        tenantAdminRead = SystemPermission.ACTION_TENANT_FILE_RESOURCE_READ.name,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_FILE_RESOURCE_UPDATE.name,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_FILE_RESOURCE_DELETE.name,
        tenantPemCreate = TenantPermission.ACTION_FILE_RESOURCE_CREATE.name,
        tenantPemRead = TenantPermission.ACTION_FILE_RESOURCE_READ.name,
        tenantPemUpdate = TenantPermission.ACTION_FILE_RESOURCE_UPDATE.name,
        tenantPemDelete = TenantPermission.ACTION_FILE_RESOURCE_DELETE.name,
    )
}
