package com.lovelycatv.template.springboot.rbac.controller.manager.permission

import com.lovelycatv.template.springboot.rbac.constants.SystemPermission
import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.SetRolePermissionsDTO
import com.lovelycatv.template.springboot.rbac.service.impl.UserRolePermissionRelationServiceImpl
import com.lovelycatv.template.springboot.shared.constants.GlobalConstants
import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.template.springboot.shared.types.UserAuthentication
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
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user-role-permission")
class ManagerUserRolePermissionController(
    private val userRolePermissionRelationService: UserRolePermissionRelationServiceImpl
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_ROLE_PERMISSION_READ}')")
    @GetMapping("/get", version = "1")
    suspend fun getRolePermissions(
        userAuthentication: UserAuthentication,
        @RequestParam roleId: Long
    ): ApiResponse<*> {
        return ApiResponse.Companion.success(userRolePermissionRelationService.getRolePermissions(roleId))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_ROLE_PERMISSION_UPDATE}')")
    @PostMapping("/set", version = "1")
    suspend fun setRolePermissions(
        userAuthentication: UserAuthentication,
        @RequestBody
        dto: SetRolePermissionsDTO
    ): ApiResponse<*> {
        userRolePermissionRelationService.setRolePermissions(dto.roleId, dto.permissionIds)
        return ApiResponse.Companion.success(null)
    }
}