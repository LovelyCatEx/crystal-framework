package com.lovelycatv.crystalframework.tenant.controller.manager.role

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.SetMemberRolesDTO
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRoleRelationService
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
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/member/role")
class ManagerTenantMemberRoleRelationController(
    private val tenantMemberRoleRelationService: TenantMemberRoleRelationService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_MEMBER_ROLE_RELATION_READ}')")
    @GetMapping("/get", version = "1")
    suspend fun getMemberRoles(
        userAuthentication: UserAuthentication,
        @RequestParam memberId: Long
    ): ApiResponse<*> {
        return ApiResponse.success(tenantMemberRoleRelationService.getMemberRoles(memberId))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_MEMBER_ROLE_RELATION_UPDATE}')")
    @PostMapping("/set", version = "1")
    suspend fun setMemberRoles(
        userAuthentication: UserAuthentication,
        @RequestBody
        @Valid
        dto: SetMemberRolesDTO
    ): ApiResponse<*> {
        tenantMemberRoleRelationService.setMemberRoles(dto.memberId, dto.roleIds)
        return ApiResponse.success(null)
    }
}