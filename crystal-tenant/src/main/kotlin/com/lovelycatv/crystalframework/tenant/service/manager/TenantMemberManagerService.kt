package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.service.BaseTenantResourceManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerDeleteTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerReadTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerUpdateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.vo.TenantMemberVO
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository

interface TenantMemberManagerService : BaseTenantResourceManagerService<
        TenantMemberRepository,
        TenantMemberEntity,
        ManagerCreateTenantMemberDTO,
        ManagerReadTenantMemberDTO,
        ManagerUpdateTenantMemberDTO,
        ManagerDeleteTenantMemberDTO
        > {
    suspend fun queryVO(dto: ManagerReadTenantMemberDTO): PaginatedResponseData<TenantMemberVO>
}
