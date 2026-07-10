package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.database.ConditionNode
import com.lovelycatv.crystalframework.shared.database.GroupNode
import com.lovelycatv.crystalframework.shared.database.QueryLogic
import com.lovelycatv.crystalframework.shared.database.QueryOperator
import com.lovelycatv.crystalframework.shared.service.BaseTenantResourceManagerService
import com.lovelycatv.crystalframework.shared.service.ScopedRelationshipCheckService
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerCreateTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerDeleteTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerReadTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerUpdateTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.vo.TenantDictItemTreeVO
import com.lovelycatv.crystalframework.tenant.entity.TenantDictItemEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDictItemRepository
import org.springframework.data.relational.core.query.Criteria

interface TenantDictItemManagerService : BaseTenantResourceManagerService<
        TenantDictItemRepository,
        TenantDictItemEntity,
        ManagerCreateTenantDictItemDTO,
        ManagerReadTenantDictItemDTO,
        ManagerUpdateTenantDictItemDTO,
        ManagerDeleteTenantDictItemDTO
        >, ScopedRelationshipCheckService {
    suspend fun getTreeByTypeId(typeId: Long): List<TenantDictItemTreeVO>

    /**
     * Resolve the root `(scope, scopeId)` from a dict type id — used by the controller in
     * create / query flows where the item does not yet exist (so [resolveRootScope] cannot be
     * called on an item id). Impl delegates to the parent DictType Service's resolver.
     */
    suspend fun resolveRootScopeFromTypeId(typeId: Long): Pair<ResourceScope, Long>?

    override suspend fun buildQueryCriteria(dto: ManagerReadTenantDictItemDTO): Criteria {
        return super.buildQueryCriteria(
            dto.copy(
                query = GroupNode(
                    logic = QueryLogic.AND,
                    children = listOfNotNull(
                        ConditionNode(
                            field = "type_id",
                            operator = QueryOperator.EQ,
                            value = dto.typeId
                        ),
                        dto.query
                    )
                )
            )
        )
    }
}
