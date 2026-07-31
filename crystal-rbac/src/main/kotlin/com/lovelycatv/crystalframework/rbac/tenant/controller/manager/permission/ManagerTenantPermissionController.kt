package com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission

import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.ManagerCreateTenantPermissionDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.ManagerDeleteTenantPermissionDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.ManagerReadTenantPermissionDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.ManagerUpdateTenantPermissionDTO
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantPermissionRepository
import com.lovelycatv.crystalframework.rbac.tenant.service.manager.TenantPermissionManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.ManagerAction
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.of
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import org.springframework.security.authorization.AuthorizationDeniedException
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
        `super` {
            create = SystemPermission.ACTION_X_TENANT_PERMISSION_CREATE.name
            read = SystemPermission.ACTION_X_TENANT_PERMISSION_READ.name
            update = SystemPermission.ACTION_X_TENANT_PERMISSION_UPDATE.name
            delete = SystemPermission.ACTION_X_TENANT_PERMISSION_DELETE.name
        }
        tenantAdmin {
            read = SystemPermission.ACTION_TENANT_PERMISSION_READ.name
        }
    },
) {
    override suspend fun authorize(
        action: ManagerAction,
        userAuthentication: UserAuthentication,
        createDto: ManagerCreateTenantPermissionDTO?,
        readDto: ManagerReadTenantPermissionDTO?,
        updateDto: ManagerUpdateTenantPermissionDTO?,
        deleteDto: ManagerDeleteTenantPermissionDTO?,
    ) {
        if (action == ManagerAction.READ || action == ManagerAction.READ_ALL) {
            val required = arrayOf(
                SystemPermission.ACTION_X_TENANT_PERMISSION_READ.name,
                SystemPermission.ACTION_TENANT_PERMISSION_READ.name,
            )
            if (!RbacUtils.hasAnyAuthority(*required)) {
                throw AuthorizationDeniedException("Access denied: required any of ${required.toList()}")
            }
            return
        }
        super.authorize(action, userAuthentication, createDto, readDto, updateDto, deleteDto)
    }
}
