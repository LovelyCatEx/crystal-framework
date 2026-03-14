package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.vo.TenantDepartmentMemberVO
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentMemberRelationRepository

interface TenantDepartmentMemberRelationService : CachedBaseService<TenantDepartmentMemberRelationRepository, TenantDepartmentMemberRelationEntity> {
    suspend fun deleteByMemberIdIn(memberIds: Collection<Long>)
}
