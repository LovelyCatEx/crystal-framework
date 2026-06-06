package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantDepartmentRepository : BaseRepository<TenantDepartmentEntity> {
    fun findAllByTenantId(tenantId: Long): Flux<TenantDepartmentEntity>

    fun findAllByTenantIdAndParentId(tenantId: Long, parentId: Long?): Flux<TenantDepartmentEntity>

    fun countByTenantId(tenantId: Long): Mono<Long>
}
