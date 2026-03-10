package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import com.lovelycatv.crystalframework.user.entity.UserEntity

interface TenantMemberRelationService : CachedBaseService<TenantMemberRepository, TenantMemberEntity> {
    suspend fun getTenantMembers(tenantId: Long): List<UserEntity>

    suspend fun setTenantMembers(tenantId: Long, userIds: List<Long>)

    suspend fun deleteByTenantIdIn(tenantIds: Collection<Long>)

    suspend fun deleteByUserIdIn(userIds: Collection<Long>)
}
