package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberProfileEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TenantMemberProfileRepository : BaseRepository<TenantMemberProfileEntity> {
    fun findByTenantMemberId(tenantMemberId: Long): Mono<TenantMemberProfileEntity>

    fun findByTenantIdAndMemberUserId(tenantId: Long, memberUserId: Long): Mono<TenantMemberProfileEntity>
}
