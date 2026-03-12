package com.lovelycatv.crystalframework.tenant.controller.manager.role

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerCreateTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerDeleteTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerReadTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerUpdateTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.service.manager.TenantRoleManagerService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/role")
class ManagerTenantRoleController(
    private val tenantRoleManagerService: TenantRoleManagerService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_ROLE_READ}')")
    @GetMapping("/list", version = "1")
    suspend fun readAll(
        userAuthentication: UserAuthentication
    ): ApiResponse<*> {
        return ApiResponse.success(tenantRoleManagerService.getRepository().findAll().awaitListWithTimeout())
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_ROLE_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateTenantRoleDTO
    ): ApiResponse<*> {
        tenantRoleManagerService.create(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_ROLE_READ}')")
    @GetMapping("/query", version = "1")
    suspend fun query(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerReadTenantRoleDTO
    ): ApiResponse<*> {
        return ApiResponse.success(tenantRoleManagerService.query(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_ROLE_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdateTenantRoleDTO
    ): ApiResponse<*> {
        tenantRoleManagerService.update(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_ROLE_DELETE}')")
    @PostMapping("/delete", version = "1")
    suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerDeleteTenantRoleDTO
    ): ApiResponse<*> {
        tenantRoleManagerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }
}
