package com.lovelycatv.crystalframework.tenant.controller.manager.member

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.SetTenantMembersDTO
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRelationService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/member")
class ManagerTenantMemberRelationController(
    private val tenantMemberRelationService: TenantMemberRelationService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_MEMBER_READ}')")
    @GetMapping("/get", version = "1")
    suspend fun getTenantMembers(
        userAuthentication: UserAuthentication,
        @RequestParam tenantId: Long
    ): ApiResponse<*> {
        return ApiResponse.success(tenantMemberRelationService.getTenantMembers(tenantId))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_MEMBER_UPDATE}')")
    @PostMapping("/set", version = "1")
    suspend fun setTenantMembers(
        userAuthentication: UserAuthentication,
        @RequestBody
        @Valid
        dto: SetTenantMembersDTO
    ): ApiResponse<*> {
        tenantMemberRelationService.setTenantMembers(dto.tenantId, dto.userIds)
        return ApiResponse.success(null)
    }
}
