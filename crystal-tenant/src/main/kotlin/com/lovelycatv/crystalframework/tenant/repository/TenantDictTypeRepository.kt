package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantDictTypeEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantDictTypeRepository : BaseRepository<TenantDictTypeEntity> {
    fun findAllByScopeId(scopeId: Long): Flux<TenantDictTypeEntity>

    fun countByScopeId(scopeId: Long): Mono<Long>

    /**
     * Resolve a dict type by its scope coordinates + business code. `(scope, scope_id, code)` is
     * the natural unique key of a dict type; used by cross-module callers (e.g. approval DICT
     * field resolution) that only know the code, not the id. `deleted_time IS NULL` is injected
     * by the framework SQL interceptor.
     */
    fun findByScopeAndScopeIdAndCode(scope: Int, scopeId: Long, code: String): Mono<TenantDictTypeEntity>
}
