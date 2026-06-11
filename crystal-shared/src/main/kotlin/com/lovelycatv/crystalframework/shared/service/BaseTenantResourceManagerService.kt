package com.lovelycatv.crystalframework.shared.service

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadTenantResourceDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.database.criteriaFromQueryNode
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.types.entity.ScopedEntity
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query

interface BaseTenantResourceManagerService<
        REPOSITORY: BaseRepository<ENTITY>,
        ENTITY,
        CREATE_DTO: Any,
        READ_DTO: BaseManagerReadTenantResourceDTO,
        UPDATE_DTO: BaseManagerUpdateDTO,
        DELETE_DTO: BaseManagerDeleteDTO
> : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
    TenantRelationshipCheckService
    where ENTITY : BaseEntity, ENTITY : ScopedEntity<Long>
{
    override suspend fun checkIsRelated(ids: Collection<Long>, parentId: Long): Boolean {
        return ids
            .map { this.getByIdOrNull(it) ?: return false }
            .all { it.getDirectParentId() == parentId }
    }

    override suspend fun buildQueryCriteria(dto: READ_DTO): Criteria {
        val tenantCriteria = if (dto.tenantId != null) {
            Criteria.where("tenant_id").`is`(dto.tenantId!!)
        } else {
            null
        }

        val queryCriteria = if (dto.query != null) {
            criteriaFromQueryNode(dto.query!!)
        } else {
            null
        }

        return when {
            tenantCriteria != null && queryCriteria != null -> tenantCriteria.and(queryCriteria)
            tenantCriteria != null -> tenantCriteria
            queryCriteria != null -> queryCriteria
            else -> Criteria.empty()
        }
    }

    suspend fun findAllByTenantId(tenantId: Long): List<ENTITY>
}
