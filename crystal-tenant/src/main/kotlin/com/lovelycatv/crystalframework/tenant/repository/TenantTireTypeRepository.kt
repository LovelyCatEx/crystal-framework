/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantTireTypeEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TenantTireTypeRepository : BaseRepository<TenantTireTypeEntity> {
    fun findByName(name: String): Mono<TenantTireTypeEntity>
}
