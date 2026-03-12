package com.lovelycatv.crystalframework.tenant.controller.manager.department

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerCreateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerDeleteTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerReadTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerUpdateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentManagerService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/department")
class ManagerTenantDepartmentController(
    private val tenantDepartmentManagerService: TenantDepartmentManagerService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_DEPARTMENT_READ}')")
    @GetMapping("/list", version = "1")
    suspend fun readAll(
        userAuthentication: UserAuthentication
    ): ApiResponse<*> {
        return ApiResponse.success(tenantDepartmentManagerService.getRepository().findAll().awaitListWithTimeout())
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_DEPARTMENT_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateTenantDepartmentDTO
    ): ApiResponse<*> {
        tenantDepartmentManagerService.create(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_DEPARTMENT_READ}')")
    @GetMapping("/query", version = "1")
    suspend fun query(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerReadTenantDepartmentDTO
    ): ApiResponse<*> {
        return ApiResponse.success(tenantDepartmentManagerService.query(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_DEPARTMENT_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdateTenantDepartmentDTO
    ): ApiResponse<*> {
        tenantDepartmentManagerService.update(dto)
        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_TENANT_DEPARTMENT_DELETE}')")
    @PostMapping("/delete", version = "1")
    suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerDeleteTenantDepartmentDTO
    ): ApiResponse<*> {
        tenantDepartmentManagerService.deleteByDTO(dto)
        return ApiResponse.success(null)
    }
}
