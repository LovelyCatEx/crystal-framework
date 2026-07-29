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
    permissions = PermissionMatrix.of {
        tenantAdmin {
            create = SystemPermission.ACTION_TENANT_PERMISSION_CREATE.name
            read = SystemPermission.ACTION_TENANT_PERMISSION_READ.name
            update = SystemPermission.ACTION_TENANT_PERMISSION_UPDATE.name
            delete = SystemPermission.ACTION_TENANT_PERMISSION_DELETE.name
        }
        tenantPem {
            create = PermissionMatrix.NEVER_GRANTED
            read = TenantPermission.ACTION_ROLE_PERMISSION_READ.name
            update = PermissionMatrix.NEVER_GRANTED
            delete = PermissionMatrix.NEVER_GRANTED
        }
    },
)
