package com.lovelycatv.crystalframework.shared.service

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadScopedDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.database.criteriaFromQueryNode
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.entity.BaseScopedEntity
import org.springframework.data.relational.core.query.Criteria

/**
 * Base service for scoped manager resources.
 *
 * Automatically injects scope-based filtering into queries: when [BaseManagerReadScopedDTO.scopeId]
 * is non-null, a `WHERE scope = ? AND scope_id = ?` criteria is added. When scopeId is null,
 * only the scope type is filtered.
 */
interface BaseScopedManagerService<
        REPOSITORY : BaseRepository<ENTITY>,
        ENTITY : BaseScopedEntity,
        CREATE_DTO : Any,
        READ_DTO : BaseManagerReadScopedDTO,
        UPDATE_DTO : BaseManagerUpdateDTO,
        DELETE_DTO : BaseManagerDeleteDTO
> : CachedBaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
    TenantRelationshipCheckService,
    ScopedRelationshipCheckService
{
    companion object {
        private const val COLUMN_SCOPE = "scope"
        private const val COLUMN_SCOPE_ID = "scope_id"
    }

    override suspend fun checkIsRelated(ids: Collection<Long>, parentId: Long): Boolean {
        return ids
            .map { this.getByIdOrNull(it) ?: return false }
            .all { it.scopeId == parentId }
    }

    /**
     * Default root-scope resolver for directly scoped entities: read `entity.scope / entity.scopeId`
     * as-is. Services whose entities are nested deeper (no `scope + scope_id` columns of their own)
     * should override this to walk up to a scoped parent Service and delegate.
     */
    override suspend fun resolveRootScope(id: Long): Pair<ResourceScope, Long>? {
        val entity = this.getByIdOrNull(id) ?: return null
        val scope = ResourceScope.getById(entity.scope) ?: return null
        return scope to entity.scopeId
    }

    override suspend fun buildQueryCriteria(dto: READ_DTO): Criteria {
        val scopeCriteria = run {
            val typeCriteria = Criteria.where(COLUMN_SCOPE).`is`(dto.scope)
            typeCriteria.and(Criteria.where(COLUMN_SCOPE_ID).`is`(dto.scopeId))
        }

        val queryCriteria = if (dto.query != null) {
            criteriaFromQueryNode(dto.query!!)
        } else {
            null
        }

        return if (queryCriteria != null) {
            scopeCriteria.and(queryCriteria)
        } else {
            scopeCriteria
        }
    }

    /**
     * Retrieve all entities belonging to a specific scope ID.
     */
    suspend fun findAllByScopeId(scopeId: Long): List<ENTITY>
}
