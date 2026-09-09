package com.lovelycatv.crystalframework.ai.repository

import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import reactor.core.publisher.Mono

interface AiProviderRepository : BaseRepository<AiProviderEntity> {
    fun findByKey(key: String): Mono<AiProviderEntity>
}
