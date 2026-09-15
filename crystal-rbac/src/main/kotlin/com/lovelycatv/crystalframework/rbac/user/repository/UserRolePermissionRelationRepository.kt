/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.user.repository

import com.lovelycatv.crystalframework.rbac.user.entity.UserRolePermissionRelationEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserRolePermissionRelationRepository : BaseRepository<UserRolePermissionRelationEntity> {
    fun findByRoleId(roleId: Long): Flux<UserRolePermissionRelationEntity>

    fun findByPermissionIdIn(permissionIds: Collection<Long>): Flux<UserRolePermissionRelationEntity>

    fun deleteByPermissionIdIn(permissionIds: Collection<Long>): Mono<Void>

    fun deleteByRoleIdIn(roleIds: Collection<Long>): Mono<Void>
}