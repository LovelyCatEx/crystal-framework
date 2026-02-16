package com.lovelycatv.template.springboot.system.repository

import com.lovelycatv.template.springboot.system.entity.SystemSettingsEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface SystemSettingsRepository : ReactiveCrudRepository<SystemSettingsEntity, Long> {
    fun findByConfigKey(configKey: String): Mono<SystemSettingsEntity>
}