package com.lovelycatv.crystalframework.tenant.controller.manager.member

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.StandardTenantManagerController
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerDeleteTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerReadTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerUpdateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/member")
class ManagerTenantMemberController(
    private val tenantMemberManagerService: TenantMemberManagerService
) : StandardTenantManagerController<
        TenantMemberManagerService,
        TenantMemberRepository,
        TenantMemberEntity,
        ManagerCreateTenantMemberDTO,
        ManagerReadTenantMemberDTO,
        ManagerUpdateTenantMemberDTO,
        ManagerDeleteTenantMemberDTO
>(
    tenantMemberManagerService,
    createPermission = SystemPermission.ACTION_TENANT_MEMBER_CREATE,
    // Tenant-scoped users cannot create members directly; the create hook below
    // enforces system-permission-only access. Members are normally added via invitations.
    scopedCreatePermission = "",
    readPermission = SystemPermission.ACTION_TENANT_MEMBER_READ,
    scopedReadPermission = TenantPermission.ACTION_TENANT_MEMBER_READ_PEM,
    updatePermission = SystemPermission.ACTION_TENANT_MEMBER_UPDATE,
    scopedUpdatePermission = TenantPermission.ACTION_TENANT_MEMBER_UPDATE_PEM,
    deletePermission = SystemPermission.ACTION_TENANT_MEMBER_DELETE,
    scopedDeletePermission = TenantPermission.ACTION_TENANT_MEMBER_DELETE_PEM
) {
    override suspend fun customCreate(
        userAuthentication: UserAuthentication,
        dto: ManagerCreateTenantMemberDTO
    ): ApiResponse<*>? {
        if (RbacUtils.hasAuthority(this.createPermission)) {
            tenantMemberManagerService.create(dto)
        } else {
            throw ForbiddenException()
        }
        return ApiResponse.success(null)
    }

    override suspend fun customQuery(
        userAuthentication: UserAuthentication,
        dto: ManagerReadTenantMemberDTO
    ): ApiResponse<*>? {
        val result = if (RbacUtils.hasAuthority(this.readPermission)) {
            tenantMemberManagerService.queryVO(dto)
        } else if (RbacUtils.hasAuthority(this.scopedReadPermission)) {
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
}
