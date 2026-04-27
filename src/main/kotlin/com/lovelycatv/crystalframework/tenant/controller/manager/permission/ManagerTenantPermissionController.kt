package com.lovelycatv.crystalframework.tenant.controller.manager.permission

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerCreateTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerDeleteTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerReadTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerUpdateTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantPermissionRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantPermissionManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = [
        SystemPermission.ACTION_TENANT_PERMISSION_READ,
        TenantPermission.ACTION_TENANT_ROLE_PERMISSION_READ_PEM,
    ],
    readAll = [
        SystemPermission.ACTION_TENANT_PERMISSION_READ,
        TenantPermission.ACTION_TENANT_ROLE_PERMISSION_READ_PEM,
    ],
    create = [SystemPermission.ACTION_TENANT_PERMISSION_CREATE],
    update = [SystemPermission.ACTION_TENANT_PERMISSION_UPDATE],
    delete = [SystemPermission.ACTION_TENANT_PERMISSION_DELETE],
)
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
>(managerService)
