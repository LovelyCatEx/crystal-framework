package com.lovelycatv.crystalframework.shared.service

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
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
     * - If [dto.id] is set → exact match on id (single result).
     * - If [dto.query] is set → build criteria from the query tree.
     * - Otherwise → no filter, simple pagination.
     */
    suspend fun query(dto: READ_DTO): PaginatedResponseData<ENTITY> {
        val limit = dto.pageSize
        val offset = (dto.page - 1) * dto.pageSize
        val template = getEntityTemplate()

        if (dto.id != null) {
            // Exact id match — use cached getByIdOrNull
            val e = this.getByIdOrNull(dto.id!!)
            return PaginatedResponseData(
                page = dto.page,
                pageSize = dto.pageSize,
                total = if (e != null) 1 else 0,
                totalPages = if (e != null) 1 else 0,
                records = if (e != null) listOf(e) else emptyList()
            )
        }

        // Build criteria
        val criteria: Criteria = buildQueryCriteria(dto)

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

    suspend fun buildQueryCriteria(dto: READ_DTO): Criteria {
        return when {
            dto.query != null -> {
                val allowlist = queryableFields()
                val used = dto.query!!.collectFields()
                val forbidden = used - allowlist
                if (forbidden.isNotEmpty()) {
                    throw BusinessException("Fields not queryable: $forbidden")
                }
                criteriaFromQueryNode(dto.query!!)
            }
            else -> {
                Criteria.empty()
            }
        }
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
