package com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardTenantManagerController
import com.lovelycatv.crystalframework.shared.controller.tenantOnly
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.ManagerCreateTenantRoleDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.ManagerDeleteTenantRoleDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.ManagerReadTenantRoleDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.ManagerUpdateTenantRoleDTO
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantRoleRepository
import com.lovelycatv.crystalframework.rbac.tenant.service.manager.TenantRoleManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/role")
class ManagerTenantRoleController(
    managerService: TenantRoleManagerService
) : StandardTenantManagerController<
        TenantRoleManagerService,
        TenantRoleRepository,
        TenantRoleEntity,
        ManagerCreateTenantRoleDTO,
        ManagerReadTenantRoleDTO,
        ManagerUpdateTenantRoleDTO,
        ManagerDeleteTenantRoleDTO
>(
    managerService,
    permissions = PermissionMatrix.tenantOnly(
        tenantAdminCreate = SystemPermission.ACTION_TENANT_ROLE_CREATE.name,
        tenantAdminRead = SystemPermission.ACTION_TENANT_ROLE_READ.name,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_ROLE_UPDATE.name,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_ROLE_DELETE.name,
        tenantPemCreate = TenantPermission.ACTION_ROLE_CREATE.name,
        tenantPemRead = TenantPermission.ACTION_ROLE_READ.name,
        tenantPemUpdate = TenantPermission.ACTION_ROLE_UPDATE.name,
        tenantPemDelete = TenantPermission.ACTION_ROLE_DELETE.name,
    ),
)
