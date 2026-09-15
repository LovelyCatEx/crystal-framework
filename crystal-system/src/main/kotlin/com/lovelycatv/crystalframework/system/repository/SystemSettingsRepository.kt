/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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