/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.resource.repository

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface StorageProviderRepository : BaseRepository<StorageProviderEntity> {
    fun findAllByActive(active: Boolean): Flux<StorageProviderEntity>

    fun findByName(name: String): Mono<StorageProviderEntity>
}
