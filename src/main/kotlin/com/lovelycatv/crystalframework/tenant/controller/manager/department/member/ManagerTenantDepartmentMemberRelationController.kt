package com.lovelycatv.crystalframework.tenant.controller.manager.department.member

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.SetDepartmentMembersDTO
import com.lovelycatv.crystalframework.tenant.service.TenantDepartmentMemberRelationService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/department/member")
class ManagerTenantDepartmentMemberRelationController(
    private val tenantDepartmentMemberRelationService: TenantDepartmentMemberRelationService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_READ}')")
    @GetMapping("/get", version = "1")
    suspend fun getDepartmentMembers(
        userAuthentication: UserAuthentication,
        @RequestParam departmentId: Long
    ): ApiResponse<*> {
        return ApiResponse.success(tenantDepartmentMemberRelationService.getDepartmentMembers(departmentId))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_UPDATE}')")
    @PostMapping("/set", version = "1")
    suspend fun setDepartmentMembers(
        userAuthentication: UserAuthentication,
        @RequestBody
        @Valid
        dto: SetDepartmentMembersDTO
    ): ApiResponse<*> {
        tenantDepartmentMemberRelationService.setDepartmentMembers(dto.departmentId, dto.memberIds)
        return ApiResponse.success(null)
    }
}
