package com.lovelycatv.crystalframework.tenant.controller.manager.department.member

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerCreateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerDeleteTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerReadTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerUpdateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentMemberManagerService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/department/member")
class ManagerTenantDepartmentMemberController(
    private val tenantDepartmentMemberManagerService: TenantDepartmentMemberManagerService,
    private val tenantDepartmentManagerService: TenantDepartmentManagerService
) {
    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateTenantDepartmentMemberDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_CREATE)) {
            tenantDepartmentMemberManagerService.create(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_CREATE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantDepartmentManagerService.checkIsRelated(dto.departmentId, userAuthentication.tenantId!!)) {
                tenantDepartmentMemberManagerService.create(dto)
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
        dto: ManagerReadTenantDepartmentMemberDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_READ)) {
            return ApiResponse.success(tenantDepartmentMemberManagerService.queryVO(dto))
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_READ_PEM)) {
            if (tenantDepartmentManagerService.checkIsRelated(dto.departmentId, userAuthentication.tenantId!!)) {
                return ApiResponse.success(tenantDepartmentMemberManagerService.queryVO(dto))
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
        dto: ManagerUpdateTenantDepartmentMemberDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_UPDATE)) {
            tenantDepartmentMemberManagerService.update(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_UPDATE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantDepartmentMemberManagerService.checkIsRelated(dto.id, userAuthentication.tenantId!!)) {
                tenantDepartmentMemberManagerService.update(dto)
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
        dto: ManagerDeleteTenantDepartmentMemberDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_DELETE)) {
            tenantDepartmentMemberManagerService.deleteByDTO(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_DELETE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantDepartmentMemberManagerService.checkIsRelated(dto.ids, userAuthentication.tenantId!!)) {
                tenantDepartmentMemberManagerService.deleteByDTO(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }
}
