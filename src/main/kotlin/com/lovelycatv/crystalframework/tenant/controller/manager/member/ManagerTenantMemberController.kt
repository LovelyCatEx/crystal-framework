package com.lovelycatv.crystalframework.tenant.controller.manager.member

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerDeleteTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerReadTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerUpdateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/member")
class ManagerTenantMemberController(
    private val tenantMemberManagerService: TenantMemberManagerService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_MEMBER_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateTenantMemberDTO
    ): ApiResponse<*> {
        tenantMemberManagerService.create(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_MEMBER_READ}')")
    @GetMapping("/query", version = "1")
    suspend fun query(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerReadTenantMemberDTO
    ): ApiResponse<*> {
        return ApiResponse.success(tenantMemberManagerService.queryVO(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_MEMBER_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdateTenantMemberDTO
    ): ApiResponse<*> {
        tenantMemberManagerService.update(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_MEMBER_DELETE}')")
    @PostMapping("/delete", version = "1")
    suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerDeleteTenantMemberDTO
    ): ApiResponse<*> {
        tenantMemberManagerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }
}
