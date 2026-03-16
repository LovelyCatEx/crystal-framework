package com.lovelycatv.crystalframework.tenant.controller.manager.role

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.SetMemberRolesDTO
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRoleRelationService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/member/role")
class ManagerTenantMemberRoleRelationController(
    private val tenantMemberRoleRelationService: TenantMemberRoleRelationService,
    private val tenantMemberManagerService: TenantMemberManagerService
) {
    @GetMapping("/get", version = "1")
    suspend fun getMemberRoles(
        userAuthentication: UserAuthentication,
        @RequestParam memberId: Long
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_MEMBER_ROLE_RELATION_READ)) {
            return ApiResponse.success(tenantMemberRoleRelationService.getMemberRoles(memberId))
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_MEMBER_ROLE_READ_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantMemberManagerService.checkIsRelated(memberId, userAuthentication.tenantId!!)) {
                return ApiResponse.success(tenantMemberRoleRelationService.getMemberRoles(memberId))
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
    }

    @PostMapping("/set", version = "1")
    suspend fun setMemberRoles(
        userAuthentication: UserAuthentication,
        @RequestBody
        @Valid
        dto: SetMemberRolesDTO
    ): ApiResponse<*> {
        if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_MEMBER_ROLE_RELATION_UPDATE)) {
            tenantMemberRoleRelationService.setMemberRoles(dto.memberId, dto.roleIds)
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_MEMBER_ROLE_UPDATE_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantMemberManagerService.checkIsRelated(dto.memberId, userAuthentication.tenantId!!)) {
                tenantMemberRoleRelationService.setMemberRoles(dto.memberId, dto.roleIds)
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }
}
