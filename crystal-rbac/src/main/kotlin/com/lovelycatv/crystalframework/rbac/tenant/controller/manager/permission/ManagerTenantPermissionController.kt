package com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.ManagerCreateTenantPermissionDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.ManagerDeleteTenantPermissionDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.ManagerReadTenantPermissionDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.ManagerUpdateTenantPermissionDTO
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantPermissionRepository
import com.lovelycatv.crystalframework.rbac.tenant.service.manager.TenantPermissionManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.of
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/permission")
class ManagerTenantPermissionController(
    managerService: TenantPermissionManagerService
) : StandardManagerController<
        TenantPermissionManagerService,
        TenantPermissionRepository,
        TenantPermissionEntity,
        ManagerCreateTenantPermissionDTO,
        ManagerReadTenantPermissionDTO,
        ManagerUpdateTenantPermissionDTO,
        ManagerDeleteTenantPermissionDTO
>(
    managerService,
    // Mixed-layer resource: legacy annotation allowed read/readAll to be satisfied by either
    // ACTION_TENANT_PERMISSION_READ (tenant.* — tenantAdmin semantics) OR
    // ACTION_TENANT_ROLE_PERMISSION_READ_PEM (i.tenant.* — tenantPem semantics).
    //
    // StandardManagerController.authorize consults layersFor(SYSTEM, op) = super + system, so
    // both permissions are placed into those two slots to keep OR-check parity. CUD keeps the
    // single ACTION_TENANT_PERMISSION_* constant in super (system = NOT_APPLICABLE).
    //
    // Prefix warnings are expected: super forbids `tenant.` / `i.tenant.` prefixes and system
    // requires `system.`; neither prefix convention matches these legacy tenant-scoped constants.
    // The warnings do not affect runtime behaviour (see PermissionMatrix.init).
    permissions = PermissionMatrix.of {
        `super` {
            create = SystemPermission.ACTION_TENANT_PERMISSION_CREATE
            read = TenantPermission.ACTION_TENANT_ROLE_PERMISSION_READ_PEM
            update = SystemPermission.ACTION_TENANT_PERMISSION_UPDATE
            delete = SystemPermission.ACTION_TENANT_PERMISSION_DELETE
        }
        system {
            create = PermissionMatrix.NOT_APPLICABLE
            read = SystemPermission.ACTION_TENANT_PERMISSION_READ
            update = PermissionMatrix.NOT_APPLICABLE
            delete = PermissionMatrix.NOT_APPLICABLE
        }
    },
)
