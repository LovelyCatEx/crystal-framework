package com.lovelycatv.crystalframework.tenant.controller.manager.tenant

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerCreateTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerDeleteTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerReadTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerUpdateTenantDTO
import com.lovelycatv.crystalframework.tenant.service.manager.TenantManagerService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant")
class ManagerTenantController(
    private val tenantManagerService: TenantManagerService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun createTenant(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateTenantDTO
    ): ApiResponse<*> {
        tenantManagerService.create(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_READ}')")
    @GetMapping("/query", version = "1")
    suspend fun queryTenant(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerReadTenantDTO
    ): ApiResponse<*> {
        return ApiResponse.success(tenantManagerService.query(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun updateTenant(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdateTenantDTO
    ): ApiResponse<*> {
        tenantManagerService.update(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_DELETE}')")
    @PostMapping("/delete", version = "1")
    suspend fun deleteTenant(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerDeleteTenantDTO
    ): ApiResponse<*> {
        tenantManagerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }
}
