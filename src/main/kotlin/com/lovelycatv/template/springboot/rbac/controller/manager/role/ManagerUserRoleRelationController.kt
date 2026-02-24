package com.lovelycatv.template.springboot.rbac.controller.manager.role

import com.lovelycatv.template.springboot.rbac.constants.SystemPermission
import com.lovelycatv.template.springboot.rbac.controller.manager.role.dto.SetUserRolesDTO
import com.lovelycatv.template.springboot.rbac.service.impl.UserRoleRelationServiceImpl
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
        return ApiResponse.Companion.success(userRoleRelationService.getUserRoles(userId))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_USER_ROLE_UPDATE}')")
    @PostMapping("/set", version = "1")
    suspend fun setUserRoles(
        userAuthentication: UserAuthentication,
        @RequestBody
        dto: SetUserRolesDTO
    ): ApiResponse<*> {
        userRoleRelationService.setUserRoles(dto.userId, dto.roleIds)
        return ApiResponse.Companion.success(null)
    }
}