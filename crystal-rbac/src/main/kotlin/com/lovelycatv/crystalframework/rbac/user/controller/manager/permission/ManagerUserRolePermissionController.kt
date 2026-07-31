package com.lovelycatv.crystalframework.rbac.user.controller.manager.permission

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto.SetRolePermissionsDTO
import com.lovelycatv.crystalframework.rbac.user.service.impl.UserRolePermissionRelationServiceImpl
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user-role-permission")
class ManagerUserRolePermissionController(
    private val userRolePermissionRelationService: UserRolePermissionRelationServiceImpl
) {
    @PreAuthorize("hasAnyAuthority('system.role.permission.read')")
    @Audit(
        action = AuditAction.READ,
        resourceType = TableConstants.TABLE_USER_ROLE_PERMISSION_RELATIONS,
        resourceIds = "#roleId",
    )
    @GetMapping("/get", version = "1")
    suspend fun getRolePermissions(
        userAuthentication: UserAuthentication,
        @RequestParam roleId: Long
    ): ApiResponse<*> {
        return ApiResponse.success(userRolePermissionRelationService.getRolePermissions(roleId))
    }

    @PreAuthorize("hasAnyAuthority('system.role.permission.update')")
    @Audit(
        action = AuditAction.UPDATE,
        resourceType = TableConstants.TABLE_USER_ROLE_PERMISSION_RELATIONS,
        resourceIds = "#dto.roleId",
    )
    @PostMapping("/set", version = "1")
    suspend fun setRolePermissions(
        userAuthentication: UserAuthentication,
        @RequestBody
        @Valid
        dto: SetRolePermissionsDTO
    ): ApiResponse<*> {
        userRolePermissionRelationService.setRolePermissions(dto.roleId, dto.permissionIds)
        return ApiResponse.success(null)
    }
}