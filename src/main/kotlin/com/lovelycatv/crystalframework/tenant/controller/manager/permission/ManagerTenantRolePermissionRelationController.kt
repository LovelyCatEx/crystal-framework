package com.lovelycatv.crystalframework.tenant.controller.manager.permission

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.SetRolePermissionsDTO
import com.lovelycatv.crystalframework.tenant.service.TenantRolePermissionRelationService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantRoleManagerService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/role/permission")
class ManagerTenantRolePermissionRelationController(
    private val tenantRolePermissionRelationService: TenantRolePermissionRelationService,
    private val tenantRoleManagerService: TenantRoleManagerService
) {
    @GetMapping("/get", version = "1")
    suspend fun getRolePermissions(
        userAuthentication: UserAuthentication,
        @RequestParam roleId: Long
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_ROLE_PERMISSION_RELATION_READ)) {
            return ApiResponse.Companion.success(tenantRolePermissionRelationService.getRolePermissions(roleId))
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_ROLE_PERMISSION_READ_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantRoleManagerService.checkIsRelated(roleId, userAuthentication.tenantId!!)) {
                return ApiResponse.Companion.success(tenantRolePermissionRelationService.getRolePermissions(roleId))
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
    }

    @PostMapping("/set", version = "1")
    suspend fun setRolePermissions(
        userAuthentication: UserAuthentication,
        @RequestBody
        @Valid
        dto: SetRolePermissionsDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_ROLE_PERMISSION_RELATION_UPDATE)) {
            tenantRolePermissionRelationService.setRolePermissions(dto.roleId, dto.permissionIds)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_ROLE_PERMISSION_UPDATE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantRoleManagerService.checkIsRelated(dto.roleId, userAuthentication.tenantId!!)) {
                tenantRolePermissionRelationService.setRolePermissions(dto.roleId, dto.permissionIds)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.Companion.success(null)
    }
}
