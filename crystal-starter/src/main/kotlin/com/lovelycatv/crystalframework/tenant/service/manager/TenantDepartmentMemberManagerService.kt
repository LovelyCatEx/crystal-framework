package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerCreateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerDeleteTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerReadTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerUpdateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.vo.TenantDepartmentMemberVO
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentMemberRelationRepository

interface TenantDepartmentMemberManagerService : BaseTenantResourceManagerService<
        TenantDepartmentMemberRelationRepository,
        TenantDepartmentMemberRelationEntity,
        ManagerCreateTenantDepartmentMemberDTO,
        ManagerReadTenantDepartmentMemberDTO,
        ManagerUpdateTenantDepartmentMemberDTO,
        ManagerDeleteTenantDepartmentMemberDTO
> {
    suspend fun queryVO(dto: ManagerReadTenantDepartmentMemberDTO): PaginatedResponseData<TenantDepartmentMemberVO>
}
