package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository

interface TenantMemberRelationService : CachedBaseService<TenantMemberRepository, TenantMemberEntity> {
    suspend fun getUserTenantMembers(userId: Long): List<TenantMemberEntity>
}
