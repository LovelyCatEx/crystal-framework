package com.lovelycatv.crystalframework.tenant.controller.manager.department.member

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerCreateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerDeleteTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerReadTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerUpdateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentMemberManagerService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/department/member")
class ManagerTenantDepartmentMemberRelationController(
    private val tenantDepartmentMemberManagerService: TenantDepartmentMemberManagerService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateTenantDepartmentMemberDTO
    ): ApiResponse<*> {
        tenantDepartmentMemberManagerService.create(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_READ}')")
    @GetMapping("/query", version = "1")
    suspend fun query(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerReadTenantDepartmentMemberDTO
    ): ApiResponse<*> {
        return ApiResponse.success(tenantDepartmentMemberManagerService.queryVO(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdateTenantDepartmentMemberDTO
    ): ApiResponse<*> {
        tenantDepartmentMemberManagerService.update(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_DELETE}')")
    @PostMapping("/delete", version = "1")
    suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerDeleteTenantDepartmentMemberDTO
    ): ApiResponse<*> {
        tenantDepartmentMemberManagerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }
}
