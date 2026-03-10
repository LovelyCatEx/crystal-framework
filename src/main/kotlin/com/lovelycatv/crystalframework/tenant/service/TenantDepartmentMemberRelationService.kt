package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentMemberRelationRepository

interface TenantDepartmentMemberRelationService : CachedBaseService<TenantDepartmentMemberRelationRepository, TenantDepartmentMemberRelationEntity> {
    suspend fun getDepartmentMembers(departmentId: Long): List<TenantMemberEntity>

    suspend fun setDepartmentMembers(departmentId: Long, memberIds: List<Long>)
}
