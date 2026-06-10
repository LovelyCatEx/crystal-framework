package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.database.ConditionNode
import com.lovelycatv.crystalframework.shared.database.GroupNode
import com.lovelycatv.crystalframework.shared.database.QueryLogic
import com.lovelycatv.crystalframework.shared.database.QueryOperator
import com.lovelycatv.crystalframework.shared.service.BaseTenantResourceManagerService
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
        > {
    suspend fun getTreeByTypeId(typeId: Long): List<TenantDictItemTreeVO>

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
