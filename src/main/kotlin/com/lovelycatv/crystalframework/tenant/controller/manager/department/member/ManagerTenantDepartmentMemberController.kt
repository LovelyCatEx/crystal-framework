package com.lovelycatv.crystalframework.tenant.controller.manager.department.member

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.StandardTenantManagerController
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerCreateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerDeleteTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerReadTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerUpdateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentMemberRelationRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentMemberManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/department/member")
class ManagerTenantDepartmentMemberController(
    private val tenantDepartmentMemberManagerService: TenantDepartmentMemberManagerService,
    private val tenantDepartmentManagerService: TenantDepartmentManagerService,
) : StandardTenantManagerController<
        TenantDepartmentMemberManagerService,
        TenantDepartmentMemberRelationRepository,
        TenantDepartmentMemberRelationEntity,
        ManagerCreateTenantDepartmentMemberDTO,
        ManagerReadTenantDepartmentMemberDTO,
        ManagerUpdateTenantDepartmentMemberDTO,
        ManagerDeleteTenantDepartmentMemberDTO
>(
    tenantDepartmentMemberManagerService,
    createPermission = SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_CREATE,
    scopedCreatePermission = TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_CREATE_PEM,
    readPermission = SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_READ,
    scopedReadPermission = TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_READ_PEM,
    updatePermission = SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_UPDATE,
    scopedUpdatePermission = TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_UPDATE_PEM,
    deletePermission = SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_DELETE,
    scopedDeletePermission = TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_DELETE_PEM,
) {
    /**
     * Department-member relations are scoped under a department, not directly under a
     * tenant — the standard `dto.tenantId == userAuthentication.tenantId` check would
     * fail (the DTO has no `tenantId`). Resolve scope by walking up via the parent
     * (department) service, which will then walk up to tenant.
     */
    override suspend fun isCreateInScope(
        dto: ManagerCreateTenantDepartmentMemberDTO,
        userAuthentication: UserAuthentication
    ): Boolean {
        return tenantDepartmentManagerService.checkIsRelatedToRootParent(
            dto.departmentId,
            userAuthentication.tenantId!!,
        )
    }

    override suspend fun isQueryInScope(
        dto: ManagerReadTenantDepartmentMemberDTO,
        userAuthentication: UserAuthentication
    ): Boolean {
        return tenantDepartmentManagerService.checkIsRelatedToRootParent(
            dto.departmentId,
            userAuthentication.tenantId!!,
        )
    }

    /**
     * Return enriched VOs (member + user info) instead of raw relation rows, keeping
     * the public response shape of `/query` unchanged from before standardization.
     */
    override suspend fun buildQueryResponse(dto: ManagerReadTenantDepartmentMemberDTO): Any {
        return tenantDepartmentMemberManagerService.queryVO(dto)
    }
}
