package com.lovelycatv.crystalframework.tenant.controller.manager.member

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.SetMemberRolesDTO
import com.lovelycatv.crystalframework.rbac.tenant.service.manager.TenantMemberRoleRelationService
import com.lovelycatv.crystalframework.rbac.tenant.service.manager.TenantRoleManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
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
    private val tenantMemberManagerService: TenantMemberManagerService,
    private val tenantRoleManagerService: TenantRoleManagerService
) {
    @GetMapping("/get", version = "1")
    suspend fun getMemberRoles(
        userAuthentication: UserAuthentication,
        @RequestParam memberId: Long
    ): ApiResponse<*> {
        return if (RbacUtils.hasAuthority(SystemPermission.ACTION_TENANT_MEMBER_ROLE_RELATION_READ)) {
            ApiResponse.success(tenantMemberRoleRelationService.getMemberRoles(memberId))
        } else if (RbacUtils.hasAuthority(TenantPermission.ACTION_TENANT_MEMBER_ROLE_READ_PEM)) {
            userAuthentication.assertTenantIdNotNull()
            if (tenantMemberManagerService.checkIsRelatedToRootParent(memberId, userAuthentication.tenantId!!)) {
                ApiResponse.success(tenantMemberRoleRelationService.getMemberRoles(memberId))
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
            val tenantId = userAuthentication.tenantId!!
            if (tenantMemberManagerService.checkIsRelatedToRootParent(dto.memberId, tenantId)) {
                if (!tenantRoleManagerService.checkIsRelatedToRootParent(dto.roleIds, tenantId)) {
                    throw ForbiddenException("Roles do not belong to your tenant")
                }
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