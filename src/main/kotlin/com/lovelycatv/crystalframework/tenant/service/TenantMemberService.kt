package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository

interface TenantMemberService : CachedBaseService<TenantMemberRepository, TenantMemberEntity> {
    suspend fun getByTenantIdAndUserId(tenantId: Long, userId: Long): TenantMemberEntity?
}
