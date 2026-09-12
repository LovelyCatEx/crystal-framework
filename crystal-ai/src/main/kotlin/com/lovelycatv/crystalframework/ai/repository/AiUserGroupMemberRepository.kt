package com.lovelycatv.crystalframework.ai.repository

import com.lovelycatv.crystalframework.ai.entity.AiUserGroupMemberEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import reactor.core.publisher.Flux

interface AiUserGroupMemberRepository : BaseRepository<AiUserGroupMemberEntity> {
    fun findAllByUserGroupId(userGroupId: Long): Flux<AiUserGroupMemberEntity>
    fun findAllByUserId(userId: Long): Flux<AiUserGroupMemberEntity>
}
