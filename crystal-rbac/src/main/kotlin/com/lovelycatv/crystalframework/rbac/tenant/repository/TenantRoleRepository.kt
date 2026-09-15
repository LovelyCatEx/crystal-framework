/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantRoleEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantRoleRepository : BaseRepository<TenantRoleEntity> {
    fun findAllByTenantId(tenantId: Long): Flux<TenantRoleEntity>

    fun findAllByTenantIdAndParentId(tenantId: Long, parentId: Long?): Flux<TenantRoleEntity>

    fun findByTenantIdAndName(tenantId: Long, name: String): Mono<TenantRoleEntity>

    fun findByName(name: String): Mono<TenantRoleEntity>

    fun findByParentId(parentId: Long): Flux<TenantRoleEntity>
    fun findAllByParentId(parentId: Long): Flux<TenantRoleEntity>
}
