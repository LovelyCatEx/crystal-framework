package com.lovelycatv.crystalframework.resource.repository

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface StorageProviderRepository : BaseRepository<StorageProviderEntity> {
    fun findAllByActive(active: Boolean): Flux<StorageProviderEntity>

    fun findByName(name: String): Mono<StorageProviderEntity>
}
