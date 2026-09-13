/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.repository

import com.lovelycatv.crystalframework.ai.entity.AiUserGroupMemberEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import reactor.core.publisher.Flux

interface AiUserGroupMemberRepository : BaseRepository<AiUserGroupMemberEntity> {
    fun findAllByUserGroupId(userGroupId: Long): Flux<AiUserGroupMemberEntity>
    fun findAllByUserId(userId: Long): Flux<AiUserGroupMemberEntity>
}
