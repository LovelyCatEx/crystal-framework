package com.lovelycatv.crystalframework.tenant.controller.manager.tiretype

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.controller.manager.tiretype.dto.ManagerCreateTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tiretype.dto.ManagerDeleteTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tiretype.dto.ManagerReadTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tiretype.dto.ManagerUpdateTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireTypeManagerService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/tire")
class ManagerTenantTireTypeController(
    private val tenantTireTypeManagerService: TenantTireTypeManagerService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_TIRE_TYPE_READ}')")
    @GetMapping("/list", version = "1")
    suspend fun readAll(
        userAuthentication: UserAuthentication
    ): ApiResponse<*> {
        return ApiResponse.success(tenantTireTypeManagerService.getRepository().findAll().awaitListWithTimeout())
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_TIRE_TYPE_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateTenantTireTypeDTO
    ): ApiResponse<*> {
        tenantTireTypeManagerService.create(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_TIRE_TYPE_READ}')")
    @GetMapping("/query", version = "1")
    suspend fun query(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerReadTenantTireTypeDTO
    ): ApiResponse<*> {
        return ApiResponse.success(tenantTireTypeManagerService.query(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_TIRE_TYPE_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdateTenantTireTypeDTO
    ): ApiResponse<*> {
        tenantTireTypeManagerService.update(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_TIRE_TYPE_DELETE}')")
    @PostMapping("/delete", version = "1")
    suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerDeleteTenantTireTypeDTO
    ): ApiResponse<*> {
        tenantTireTypeManagerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }
}
