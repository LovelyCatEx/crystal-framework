package com.lovelycatv.crystalframework.shared.service

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode
import com.lovelycatv.crystalframework.shared.database.collectFields
import com.lovelycatv.crystalframework.shared.database.criteriaFromQueryNode
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.utils.EntityQueryableFieldsResolver
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query

interface BaseManagerService<
        REPOSITORY: BaseRepository<ENTITY>,
        ENTITY: BaseEntity,
        CREATE_DTO: Any,
        READ_DTO: BaseManagerReadDTO,
        UPDATE_DTO: BaseManagerUpdateDTO,
        DELETE_DTO: BaseManagerDeleteDTO
> : BaseService<REPOSITORY, ENTITY> {

    /**
     * Provide the [R2dbcEntityTemplate] for programmatic criteria-based queries.
     *
     * Override this in your service implementation by injecting and returning the
     * [R2dbcEntityTemplate] bean.
     */
    fun getEntityTemplate(): R2dbcEntityTemplate

    /**
     * Unified query method. All filtering is done via [QueryNode] criteria.
     *
     * - The client-supplied [dto.query] is validated against [queryableFields] **up front**, in this
     *   method, before delegating to [buildQueryCriteria]. Because the check lives here (and not
     *   inside the overridable [buildQueryCriteria]), no subclass override can bypass the allowlist
     *   — closing the field-allowlist bypass in the scoped / tenant-resource services.
     * - If [dto.id] is set → exact match on id, **AND-ed with** the isolation criteria produced by
     *   [buildQueryCriteria] (tenant / scope filters). This closes the cross-tenant IDOR where an
     *   id lookup would otherwise bypass [buildQueryCriteria] entirely and return any entity by id
     *   regardless of the caller's tenant / scope.
     * - If [dto.query] is set → build criteria from the query tree.
     * - Otherwise → no filter, simple pagination.
     */
    suspend fun query(dto: READ_DTO): PaginatedResponseData<ENTITY> {
        val limit = dto.pageSize
        val offset = (dto.page - 1) * dto.pageSize
        val template = getEntityTemplate()

        // Enforce the queryable-fields allowlist on the raw client query up front. This runs before
        // any subclass buildQueryCriteria override, so scoped / tenant-resource services can no
        // longer skip it. Subclass-injected criteria (tenant_id / scope / type_id) are built inside
        // buildQueryCriteria — they never pass through dto.query, so they are unaffected.
        validateQueryableFields(dto.query)

        // Always apply the isolation criteria (tenant / scope) from buildQueryCriteria. When an id
        // is supplied, AND it onto that criteria so the id lookup stays scoped to the caller's
        // tenant / scope instead of short-circuiting past the filter.
        val criteria: Criteria = buildQueryCriteria(dto).let { isolation ->
            val id = dto.id ?: return@let isolation
            val idCriteria = Criteria.where(BaseEntity.COLUMN_ID).`is`(id)
            if (isolation.isEmpty) idCriteria else isolation.and(idCriteria)
        }

        val baseQuery = Query.query(criteria)
            .sort(Sort.by(Sort.Direction.DESC, "created_time"))

        val total = template
            .count(baseQuery, entityClass.java)
            .awaitFirstOrNull() ?: 0L

        val records = template
            .select(baseQuery.limit(limit).offset(offset.toLong()), entityClass.java)
            .collectList()
            .awaitFirstOrNull() ?: emptyList()

        return dto.toPaginatedResponseData(
            total = total,
            records = records
        )
    }

    /**
     * Validate that every field referenced by the client-supplied [query] tree is in the
     * [queryableFields] allowlist. `private` on purpose: subclasses tune *which* fields are
     * queryable by overriding [queryableFields], but they cannot bypass the *enforcement* here.
     */
    private fun validateQueryableFields(query: QueryNode?) {
        if (query == null) return
        val forbidden = query.collectFields() - queryableFields()
        if (forbidden.isNotEmpty()) {
            throw BusinessException("Fields not queryable: $forbidden")
        }
    }

    suspend fun buildQueryCriteria(dto: READ_DTO): Criteria {
        return if (dto.query != null) criteriaFromQueryNode(dto.query!!) else Criteria.empty()
    }

    /**
     * Fields exposed to `QueryNode`-based filtering. Defaults to `@Column` fields
     * on the entity minus any `@NotQueryable`. Override to further restrict.
     */
    fun queryableFields(): Set<String> = EntityQueryableFieldsResolver.resolve(entityClass)

    suspend fun create(dto: CREATE_DTO): ENTITY

    suspend fun update(dto: UPDATE_DTO): ENTITY? {
        val existing = this.getByIdOrNull(dto.id) ?: return null

        val pre = this.applyDTOToEntity(dto, existing)

        val result = this.getRepository().save(
            pre.apply {
                this.modifiedTime = System.currentTimeMillis()
            } newEntity false
        ).awaitFirstOrNull() ?: throw BusinessException("Could not update resource")

        return result
    }

    suspend fun applyDTOToEntity(dto: UPDATE_DTO, original: ENTITY): ENTITY

    suspend fun batchDelete(ids: List<Long>) {
        try {
            this.getRepository().deleteAllById(ids).awaitFirstOrNull()
        } catch (_: Exception) {
            throw BusinessException("Could not delete resources")
        }
    }

    suspend fun delete(id: Long) {
        try {
            this.batchDelete(listOf(id))
        } catch (_: Exception) {
            throw BusinessException("Could not delete resource")
        }
    }

    suspend fun deleteByDTO(dto: DELETE_DTO) {
        this.batchDelete(dto.ids)
    }
}
