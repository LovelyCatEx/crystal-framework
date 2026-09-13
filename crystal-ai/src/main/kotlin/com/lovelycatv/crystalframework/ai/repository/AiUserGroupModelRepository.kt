package com.lovelycatv.crystalframework.ai.repository

import com.lovelycatv.crystalframework.ai.entity.AiUserGroupModelEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AiUserGroupModelRepository : BaseRepository<AiUserGroupModelEntity> {
    fun findAllByUserGroupId(userGroupId: Long): Flux<AiUserGroupModelEntity>
    fun findAllByModelId(modelId: Long): Flux<AiUserGroupModelEntity>
    fun existsByUserGroupIdAndModelId(userGroupId: Long, modelId: Long): Mono<Boolean>
}
