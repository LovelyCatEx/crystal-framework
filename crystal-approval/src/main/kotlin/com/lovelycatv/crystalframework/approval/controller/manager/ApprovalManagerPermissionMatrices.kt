package com.lovelycatv.crystalframework.approval.controller.manager

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix

/**
 * Single source of truth for the permission matrices of the approval module's Manager Controller
 * family.
 *
 * Declares the [DEFINITION] (flow definition) and [INSTANCE] (flow instance) [PermissionMatrix]
 * instances, referenced by the corresponding Controllers so the same authority bits are not
 * maintained in multiple places.
 */
object ApprovalManagerPermissionMatrices {

    /** Four-layer permission matrix for flow definitions (full CRUD). */
    val DEFINITION: PermissionMatrix = PermissionMatrix(
        superCreate = SystemPermission.ACTION_X_APPROVAL_FLOW_DEFINITION_CREATE.name,
        superRead = SystemPermission.ACTION_X_APPROVAL_FLOW_DEFINITION_READ.name,
        superUpdate = SystemPermission.ACTION_X_APPROVAL_FLOW_DEFINITION_UPDATE.name,
        superDelete = SystemPermission.ACTION_X_APPROVAL_FLOW_DEFINITION_DELETE.name,
        systemCreate = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_DELETE.name,
        tenantAdminCreate = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE.name,
        tenantAdminRead = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ.name,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE.name,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE.name,
        tenantPemCreate = TenantPermission.ACTION_APPROVAL_FLOW_DEFINITION_CREATE.name,
        tenantPemRead = TenantPermission.ACTION_APPROVAL_FLOW_DEFINITION_READ.name,
        tenantPemUpdate = TenantPermission.ACTION_APPROVAL_FLOW_DEFINITION_UPDATE.name,
        tenantPemDelete = TenantPermission.ACTION_APPROVAL_FLOW_DEFINITION_DELETE.name,
    )

    /** Four-layer permission matrix for flow instances (read-only). */
    val INSTANCE: PermissionMatrix = PermissionMatrix.readonly(
        superRead = SystemPermission.ACTION_X_APPROVAL_FLOW_INSTANCE_READ.name,
        systemRead = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_INSTANCE_READ.name,
        tenantAdminRead = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ.name,
        tenantPemRead = TenantPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ.name,
    )
}
