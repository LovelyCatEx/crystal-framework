package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantMemberRepository : BaseRepository<TenantMemberEntity> {
    fun findAllByTenantId(tenantId: Long): Flux<TenantMemberEntity>

    fun findAllByMemberUserId(memberUserId: Long): Flux<TenantMemberEntity>

    fun findByTenantIdAndMemberUserId(tenantId: Long, memberUserId: Long): Mono<TenantMemberEntity>

    fun countByTenantId(tenantId: Long): Mono<Long>

    @Query("SELECT COUNT(*) FROM tenant_members WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}
