package com.lovelycatv.crystalframework.ai.repository

import com.lovelycatv.crystalframework.ai.entity.AiUserGroupEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import reactor.core.publisher.Mono

interface AiUserGroupRepository : BaseRepository<AiUserGroupEntity> {
    fun findByKey(key: String): Mono<AiUserGroupEntity>
}
