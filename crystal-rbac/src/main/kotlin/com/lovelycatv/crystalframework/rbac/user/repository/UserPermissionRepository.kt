/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.user.repository

import com.lovelycatv.crystalframework.rbac.user.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserPermissionRepository : BaseRepository<UserPermissionEntity> {
    fun id(id: Long): MutableList<UserPermissionEntity>

    fun findByName(name: String): Mono<UserPermissionEntity>
}
