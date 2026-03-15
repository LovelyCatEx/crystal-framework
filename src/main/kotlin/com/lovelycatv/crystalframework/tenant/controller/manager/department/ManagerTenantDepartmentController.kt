package com.lovelycatv.crystalframework.tenant.controller.manager.department

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerCreateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerDeleteTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerReadTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerUpdateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentManagerService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/department")
class ManagerTenantDepartmentController(
    private val tenantDepartmentManagerService: TenantDepartmentManagerService
) {
    @GetMapping("/list", version = "1")
    suspend fun readAll(
        userAuthentication: UserAuthentication,
        @RequestParam
        tenantId: Long,
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_DEPARTMENT_READ)) {
            return ApiResponse.success(tenantDepartmentManagerService.getRepository().findAllByTenantId(tenantId).awaitListWithTimeout())
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_DEPARTMENT_READ_PEM)) {
            if (tenantId == userAuthentication.tenantId) {
                return ApiResponse.success(tenantDepartmentManagerService.getRepository().findAllByTenantId(tenantId).awaitListWithTimeout())
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
    }

    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateTenantDepartmentDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_DEPARTMENT_CREATE)) {
            tenantDepartmentManagerService.create(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_DEPARTMENT_CREATE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (dto.tenantId == userAuthentication.tenantId) {
                tenantDepartmentManagerService.create(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }

    @GetMapping("/query", version = "1")
    suspend fun query(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerReadTenantDepartmentDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_DEPARTMENT_READ)) {
            return ApiResponse.success(tenantDepartmentManagerService.query(dto))
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_DEPARTMENT_READ_PEM)) {
            if (dto.tenantId == userAuthentication.tenantId) {
                return ApiResponse.success(tenantDepartmentManagerService.query(dto))
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
    }

    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdateTenantDepartmentDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_DEPARTMENT_UPDATE)) {
            tenantDepartmentManagerService.update(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_DEPARTMENT_UPDATE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantDepartmentManagerService.checkIsRelated(dto.id, userAuthentication.tenantId!!)) {
                tenantDepartmentManagerService.update(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }

    @PostMapping("/delete", version = "1")
    suspend fun delete(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerDeleteTenantDepartmentDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_DEPARTMENT_DELETE)) {
            tenantDepartmentManagerService.deleteByDTO(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_DEPARTMENT_DELETE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantDepartmentManagerService.checkIsRelated(dto.ids, userAuthentication.tenantId!!)) {
                tenantDepartmentManagerService.deleteByDTO(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }
}
