package com.lovelycatv.crystalframework.tenant.controller.manager.member

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardTenantManagerController
import com.lovelycatv.crystalframework.shared.controller.tenantOnly
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
    permissions = PermissionMatrix.tenantOnly(
        tenantAdminCreate = SystemPermission.ACTION_TENANT_MEMBER_CREATE,
        tenantAdminRead = SystemPermission.ACTION_TENANT_MEMBER_READ,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_MEMBER_UPDATE,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_MEMBER_DELETE,
        // Tenant-scoped users cannot create members directly; new members must come
        // through the invitation flow. NEVER_GRANTED makes the layer exist but never
        // match, so the standard authorize path deterministically denies.
        tenantPemCreate = PermissionMatrix.NEVER_GRANTED,
        tenantPemRead = TenantPermission.ACTION_TENANT_MEMBER_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_MEMBER_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_MEMBER_DELETE_PEM,
    ),
) {
    /**
     * Return enriched VOs (member + user info) instead of raw member rows, preserving the
     * public response shape of `/query` that the pre-migration `customQuery` override provided.
     */
    override suspend fun buildQueryResponse(dto: ManagerReadTenantMemberDTO): Any {
        return tenantMemberManagerService.queryVO(dto)
    }
}
