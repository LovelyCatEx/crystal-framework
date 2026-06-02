package com.lovelycatv.crystalframework.tenant.settings.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.settings.entity.TenantSettingsEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantSettingsRepository : BaseRepository<TenantSettingsEntity> {
    fun findByTenantIdAndConfigKey(tenantId: Long, configKey: String): Mono<TenantSettingsEntity>

    fun findAllByTenantId(tenantId: Long): Flux<TenantSettingsEntity>

    fun findAllByTenantIdAndConfigKeyIn(
        tenantId: Long,
        configKeys: Collection<String>,
    ): Flux<TenantSettingsEntity>
}
