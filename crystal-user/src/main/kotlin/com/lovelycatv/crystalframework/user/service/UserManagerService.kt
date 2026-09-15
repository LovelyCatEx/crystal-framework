/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerCreateUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerDeleteUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerReadUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerUpdateUserDTO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository

interface UserManagerService : CachedBaseManagerService<
        UserRepository,
        UserEntity,
        ManagerCreateUserDTO,
        ManagerReadUserDTO,
        ManagerUpdateUserDTO,
        ManagerDeleteUserDTO
> {
    /**
     * Enable or disable a user account by flipping [UserEntity.getEnabledFlag].
     */
    suspend fun setEnabled(userId: Long, enabled: Boolean)
}
