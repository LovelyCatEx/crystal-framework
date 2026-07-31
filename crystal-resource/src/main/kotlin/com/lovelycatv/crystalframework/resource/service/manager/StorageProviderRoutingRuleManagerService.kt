package com.lovelycatv.crystalframework.resource.service.manager

import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerCreateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerDeleteStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerReadStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.ManagerUpdateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.dto.SimulateStorageProviderRoutingRuleDTO
import com.lovelycatv.crystalframework.resource.controller.manager.routing.vo.SimulationResultVO
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

    /** Dry-run route resolution: builds a RoutingContext from [dto], asks the router to simulate,
     *  and maps the result to VO. Used by the simulate management endpoint. */
    suspend fun simulate(dto: SimulateStorageProviderRoutingRuleDTO): SimulationResultVO
}
