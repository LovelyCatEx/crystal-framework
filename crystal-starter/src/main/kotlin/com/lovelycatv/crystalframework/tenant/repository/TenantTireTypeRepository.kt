package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantTireTypeEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TenantTireTypeRepository : BaseRepository<TenantTireTypeEntity> {
    fun findByName(name: String): Mono<TenantTireTypeEntity>
}
