package com.lovelycatv.crystalframework.tenant.controller.manager.role

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.StandardTenantManagerController
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerCreateTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerDeleteTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerReadTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerUpdateTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRoleRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantRoleManagerService
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
    createPermission = SystemPermission.ACTION_TENANT_ROLE_CREATE,
    scopedCreatePermission = TenantPermission.ACTION_TENANT_ROLE_CREATE_PEM,
    readPermission = SystemPermission.ACTION_TENANT_ROLE_READ,
    scopedReadPermission = TenantPermission.ACTION_TENANT_ROLE_READ_PEM,
    updatePermission = SystemPermission.ACTION_TENANT_ROLE_UPDATE,
    scopedUpdatePermission = TenantPermission.ACTION_TENANT_ROLE_UPDATE_PEM,
    deletePermission = SystemPermission.ACTION_TENANT_ROLE_DELETE,
    scopedDeletePermission = TenantPermission.ACTION_TENANT_ROLE_DELETE_PEM
)
