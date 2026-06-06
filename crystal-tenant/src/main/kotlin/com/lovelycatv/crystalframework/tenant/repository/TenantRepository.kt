package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantRepository : BaseRepository<TenantEntity> {
    fun findByName(name: String): Mono<TenantEntity>

    fun findAllByOwnerUserId(ownerUserId: Long): Flux<TenantEntity>

    @Query("SELECT COUNT(*) FROM tenants WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}
