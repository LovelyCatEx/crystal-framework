package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantPermissionEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantPermissionRepository : BaseRepository<TenantPermissionEntity> {
    fun findByName(name: String): Mono<TenantPermissionEntity>

    fun findByNameIn(names: Collection<String>): Flux<TenantPermissionEntity>
}
