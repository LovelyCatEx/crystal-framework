package com.lovelycatv.crystalframework.resource.repository

import com.lovelycatv.crystalframework.resource.entity.StorageProviderRoutingRuleEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface StorageProviderRoutingRuleRepository : BaseRepository<StorageProviderRoutingRuleEntity> {
    fun findAllByEnabledOrderByPriorityAsc(enabled: Boolean): Flux<StorageProviderRoutingRuleEntity>
}
