package com.lovelycatv.crystalframework.resource.service

import com.lovelycatv.crystalframework.resource.entity.StorageProviderRoutingRuleEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRoutingRuleRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface StorageProviderRoutingRuleService : CachedBaseService<StorageProviderRoutingRuleRepository, StorageProviderRoutingRuleEntity> {
    /** Enabled rules sorted by priority ascending, cached — the routing hot path reads this. */
    suspend fun getActiveRulesSortedByPriority(): List<StorageProviderRoutingRuleEntity>
}
