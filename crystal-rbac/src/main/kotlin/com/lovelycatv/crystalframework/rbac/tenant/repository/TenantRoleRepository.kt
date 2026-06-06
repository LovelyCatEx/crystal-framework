package com.lovelycatv.crystalframework.rbac.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantRoleEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantRoleRepository : BaseRepository<TenantRoleEntity> {
    fun findAllByTenantId(tenantId: Long): Flux<TenantRoleEntity>

    fun findAllByTenantIdAndParentId(tenantId: Long, parentId: Long?): Flux<TenantRoleEntity>

    fun findByTenantIdAndName(tenantId: Long, name: String): Mono<TenantRoleEntity>

    fun findByName(name: String): Mono<TenantRoleEntity>

    fun findByParentId(parentId: Long): Flux<TenantRoleEntity>
    fun findAllByParentId(parentId: Long): Flux<TenantRoleEntity>
}
