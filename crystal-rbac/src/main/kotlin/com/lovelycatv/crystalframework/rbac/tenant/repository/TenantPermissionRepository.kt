/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantPermissionEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantPermissionRepository : BaseRepository<TenantPermissionEntity> {
    fun findByName(name: String): Mono<TenantPermissionEntity>

    fun findByNameIn(names: Collection<String>): Flux<TenantPermissionEntity>
}
