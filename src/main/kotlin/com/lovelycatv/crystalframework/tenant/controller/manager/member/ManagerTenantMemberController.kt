package com.lovelycatv.crystalframework.tenant.controller.manager.member

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerDeleteTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerReadTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerUpdateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/member")
class ManagerTenantMemberController(
    private val tenantMemberManagerService: TenantMemberManagerService
) {
    @PostMapping("/create", version = "1")
    suspend fun create(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateTenantMemberDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_MEMBER_CREATE)) {
            tenantMemberManagerService.create(dto)
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
        dto: ManagerReadTenantMemberDTO
    ): ApiResponse<*> {
        val result = if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_MEMBER_READ)) {
            tenantMemberManagerService.queryVO(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_MEMBER_READ_PEM)) {
            if (dto.tenantId == userAuthentication.tenantId) {
                tenantMemberManagerService.queryVO(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }

        return ApiResponse.success(result)
    }

    @PostMapping("/update", version = "1")
    suspend fun update(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdateTenantMemberDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_MEMBER_UPDATE)) {
            tenantMemberManagerService.update(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_MEMBER_UPDATE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantMemberManagerService.checkIsRelated(dto.id, userAuthentication.tenantId!!)) {
                tenantMemberManagerService.update(dto)
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
        dto: ManagerDeleteTenantMemberDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_MEMBER_DELETE)) {
            tenantMemberManagerService.deleteByDTO(dto)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_MEMBER_DELETE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantMemberManagerService.checkIsRelated(dto.ids, userAuthentication.tenantId!!)) {
                tenantMemberManagerService.deleteByDTO(dto)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }

        return ApiResponse.success(null)
    }
}
