/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.repository

import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AiModelRepository : BaseRepository<AiModelEntity> {
    fun findByProviderIdAndKey(providerId: Long, key: String): Mono<AiModelEntity>
    fun findAllByProviderId(providerId: Long): Flux<AiModelEntity>
}
