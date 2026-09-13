/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantDictItemEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantDictItemRepository : BaseRepository<TenantDictItemEntity> {
    fun findAllByTypeId(typeId: Long): Flux<TenantDictItemEntity>

    fun findAllByTypeIdAndParentId(typeId: Long, parentId: Long?): Flux<TenantDictItemEntity>

    fun countByTypeId(typeId: Long): Mono<Long>
}
