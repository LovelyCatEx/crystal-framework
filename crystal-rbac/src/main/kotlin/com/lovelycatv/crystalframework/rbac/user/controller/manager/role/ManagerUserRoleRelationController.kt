package com.lovelycatv.crystalframework.rbac.user.controller.manager.role

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.SetUserRolesDTO
import com.lovelycatv.crystalframework.rbac.user.service.impl.UserRoleRelationServiceImpl
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
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
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user-role-relation")
class ManagerUserRoleRelationController(
    private val userRoleRelationService: UserRoleRelationServiceImpl
) {
    @RequiresAuthority(anyOf = ["system.user.role.read"], scope = ResourceScope.SYSTEM)
    @Audit(
        action = AuditAction.READ,
        resourceType = TableConstants.TABLE_USER_ROLE_RELATIONS,
        resourceIds = "#userId",
    )
    @GetMapping("/get", version = "1")
    suspend fun getUserRoles(
        userAuthentication: UserAuthentication,
        @RequestParam userId: Long
    ): ApiResponse<*> {
        return ApiResponse.success(userRoleRelationService.getUserRoles(userId))
    }

    @RequiresAuthority(anyOf = ["system.user.role.update"], scope = ResourceScope.SYSTEM)
    @Audit(
        action = AuditAction.UPDATE,
        resourceType = TableConstants.TABLE_USER_ROLE_RELATIONS,
        resourceIds = "#dto.userId",
    )
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