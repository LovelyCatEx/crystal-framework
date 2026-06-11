package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantDictTypeEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantDictTypeRepository : BaseRepository<TenantDictTypeEntity> {
    fun findAllByTenantId(tenantId: Long): Flux<TenantDictTypeEntity>

    fun countByTenantId(tenantId: Long): Mono<Long>
}
