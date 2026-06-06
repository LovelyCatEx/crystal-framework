package com.lovelycatv.crystalframework.rbac.user.controller.manager.role

import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.SetUserRolesDTO
import com.lovelycatv.crystalframework.rbac.user.service.impl.UserRoleRelationServiceImpl
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
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
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user-role-relation")
class ManagerUserRoleRelationController(
    private val userRoleRelationService: UserRoleRelationServiceImpl
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_USER_ROLE_READ}')")
    @GetMapping("/get", version = "1")
    suspend fun getUserRoles(
        userAuthentication: UserAuthentication,
        @RequestParam userId: Long
    ): ApiResponse<*> {
        return ApiResponse.success(userRoleRelationService.getUserRoles(userId))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_USER_ROLE_UPDATE}')")
    @PostMapping("/set", version = "1")
    suspend fun setUserRoles(
        userAuthentication: UserAuthentication,
        @RequestBody
        @Valid
        dto: SetUserRolesDTO
    ): ApiResponse<*> {
        userRoleRelationService.setUserRoles(dto.userId, dto.roleIds)
        return ApiResponse.success(null)
    }
}