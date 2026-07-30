package com.lovelycatv.crystalframework.resource.service.manager

import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerCreateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerDeleteStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerReadStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerUpdateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.entity.StorageProviderRoutingRuleEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRoutingRuleRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface StorageProviderRoutingRuleManagerService : CachedBaseManagerService<
        StorageProviderRoutingRuleRepository,
        StorageProviderRoutingRuleEntity,
        ManagerCreateStorageProviderRoutingRuleDTO,
        ManagerReadStorageProviderRoutingRuleDTO,
        ManagerUpdateStorageProviderRoutingRuleDTO,
        ManagerDeleteStorageProviderRoutingRuleDTO
> {
    /** Batch-reorders rules by writing `priority` = index within [orderedIds], then invalidates the routing cache. */
    suspend fun reorder(orderedIds: List<Long>)
}
