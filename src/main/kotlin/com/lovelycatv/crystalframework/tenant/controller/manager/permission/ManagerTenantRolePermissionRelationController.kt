package com.lovelycatv.crystalframework.tenant.controller.manager.permission

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.SetRolePermissionsDTO
import com.lovelycatv.crystalframework.tenant.service.TenantRolePermissionRelationService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
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
    private val tenantRolePermissionRelationService: TenantRolePermissionRelationService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_ROLE_PERMISSION_RELATION_READ}')")
    @GetMapping("/get", version = "1")
    suspend fun getRolePermissions(
        userAuthentication: UserAuthentication,
        @RequestParam roleId: Long
    ): ApiResponse<*> {
        return ApiResponse.Companion.success(tenantRolePermissionRelationService.getRolePermissions(roleId))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_ROLE_PERMISSION_RELATION_UPDATE}')")
    @PostMapping("/set", version = "1")
    suspend fun setRolePermissions(
        userAuthentication: UserAuthentication,
        @RequestBody
        @Valid
        dto: SetRolePermissionsDTO
    ): ApiResponse<*> {
        tenantRolePermissionRelationService.setRolePermissions(dto.roleId, dto.permissionIds)
        return ApiResponse.Companion.success(null)
    }
}