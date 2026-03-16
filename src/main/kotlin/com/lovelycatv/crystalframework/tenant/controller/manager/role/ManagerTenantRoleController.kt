package com.lovelycatv.crystalframework.tenant.controller.manager.role

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerCreateTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerDeleteTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerReadTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerUpdateTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.service.manager.TenantRoleManagerService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/role")
class ManagerTenantRoleController(
    private val tenantRoleManagerService: TenantRoleManagerService
) {
    @GetMapping("/list", version = "1")
    suspend fun readAll(
        userAuthentication: UserAuthentication,
        @RequestParam
        tenantId: Long,
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_ROLE_READ)) {
            return ApiResponse.success(tenantRoleManagerService.getRepository().findAllByTenantId(tenantId).awaitListWithTimeout())
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_ROLE_READ_PEM)) {
            if (tenantId == userAuthentication.tenantId) {
                return ApiResponse.success(tenantRoleManagerService.getRepository().findAllByTenantId(tenantId).awaitListWithTimeout())
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
        dto: ManagerCreateTenantRoleDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_ROLE_CREATE)) {
            tenantRoleManagerService.create(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_ROLE_CREATE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (dto.tenantId == userAuthentication.tenantId) {
                tenantRoleManagerService.create(dto)
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
        dto: ManagerReadTenantRoleDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_ROLE_READ)) {
            return ApiResponse.success(tenantRoleManagerService.query(dto))
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_ROLE_READ_PEM)) {
            if (dto.tenantId == userAuthentication.tenantId) {
                return ApiResponse.success(tenantRoleManagerService.query(dto))
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
        dto: ManagerUpdateTenantRoleDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_ROLE_UPDATE)) {
            tenantRoleManagerService.update(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_ROLE_UPDATE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantRoleManagerService.checkIsRelated(dto.id, userAuthentication.tenantId!!)) {
                tenantRoleManagerService.update(dto)
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
        dto: ManagerDeleteTenantRoleDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_ROLE_DELETE)) {
            tenantRoleManagerService.deleteByDTO(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_ROLE_DELETE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantRoleManagerService.checkIsRelated(dto.ids, userAuthentication.tenantId!!)) {
                tenantRoleManagerService.deleteByDTO(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }
}
