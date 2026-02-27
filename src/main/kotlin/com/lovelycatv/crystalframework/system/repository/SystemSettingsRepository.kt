package com.lovelycatv.crystalframework.system.repository

import com.lovelycatv.crystalframework.system.entity.SystemSettingsEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface SystemSettingsRepository : R2dbcRepository<SystemSettingsEntity, Long> {
    fun findByConfigKey(configKey: String): Mono<SystemSettingsEntity>

    fun findAllByConfigKeyIn(configKeys: Collection<String>): Flux<SystemSettingsEntity>
}