package com.lovelycatv.crystalframework.shared.service

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.database.criteriaFromQueryNode
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
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
     * [R2dbcEntityTemplate] bean. The default throws [UnsupportedOperationException]
     * so that services which never use [query][BaseManagerReadDTO.query]-based queries
     * are unaffected.
     */
    fun getEntityTemplate(): R2dbcEntityTemplate {
        throw UnsupportedOperationException(
            "${this::class.simpleName} does not provide an R2dbcEntityTemplate. " +
            "Override getEntityTemplate() to enable query-node-based queries."
        )
    }

    /**
     * Returns the entity class for use with [R2dbcEntityTemplate].
     *
     * Override this in your service implementation. The default throws
     * [UnsupportedOperationException] so that services which never use
     * [query][BaseManagerReadDTO.query]-based queries are unaffected.
     */
    fun getEntityClass(): Class<ENTITY> {
        throw UnsupportedOperationException(
            "${this::class.simpleName} does not provide an entity class. " +
            "Override getEntityClass() to enable query-node-based queries."
        )
    }

    suspend fun query(
        dto: READ_DTO,
        isAdvanceQuery: suspend (dto: READ_DTO) -> Boolean = { dto.searchKeyword != null },
        doAdvanceQuery: suspend (dto: READ_DTO, limit: Int, offset: Int) -> PaginatedResponseData<ENTITY> = this::defaultAdvanceQuery
    ): PaginatedResponseData<ENTITY> {
        return if (dto.id != null) {
            // Find by id
            val e = this.getByIdOrNull(dto.id!!)
            PaginatedResponseData(
                page = dto.page,
                pageSize = dto.pageSize,
                total = if (e != null) 1 else 0,
                totalPages = if (e != null) 1 else 0,
                records = if (e != null) listOf(e) else emptyList()
            )
        } else {
            val limit = dto.pageSize
            val offset = (dto.page - 1) * dto.pageSize

            // QueryNode tree takes highest priority over legacy advance query
            if (dto.query != null) {
                return queryWithNode(dto, limit, offset)
            }

            if (isAdvanceQuery.invoke(dto)) {
                doAdvanceQuery.invoke(dto, limit, offset)
            } else {
                // Simple pagination
                val hasTimeRange = dto.startTime != null && dto.endTime != null

                val total = if (hasTimeRange) {
                    this.getRepository().countWithTimeRange(dto.startTime!!, dto.endTime!!).awaitFirstOrNull() ?: 0
                } else {
                    this.getRepository().count().awaitFirstOrNull() ?: 0
                }

                val records = if (hasTimeRange) {
                    this.getRepository()
                        .findAllByPageWithTimeRange(dto.startTime!!, dto.endTime!!, limit, offset)
                        .awaitListWithTimeout()
                } else {
                    this.getRepository()
                        .findAllByPage(limit, offset)
                        .awaitListWithTimeout()
                }

                dto.toPaginatedResponseData(
                    total = total,
                    records = records
                )
            }
        }
    }

    /**
     * Execute a programmatic query by converting [dto.query] into a [Criteria]
     * tree via [criteriaFromQueryNode], then running it through [R2dbcEntityTemplate].
     */
    private suspend fun queryWithNode(
        dto: READ_DTO,
        limit: Int,
        offset: Int
    ): PaginatedResponseData<ENTITY> {
        val criteria = criteriaFromQueryNode(dto.query!!)
        val template = getEntityTemplate()
        val entityClass = getEntityClass()

        val total = template
            .count(Query.query(criteria), entityClass)
            .awaitFirstOrNull() ?: 0L

        val records = template
            .select(
                Query.query(criteria).limit(limit).offset(offset.toLong()),
                entityClass
            )
            .collectList()
            .awaitFirstOrNull() ?: emptyList()

        return dto.toPaginatedResponseData(
            total = total,
            records = records
        )
    }

    /**
     * Fix: Caused by: java.lang.AssertionError: FUN LOCAL_FUNCTION_FOR_LAMBDA
     *
     *     name:<anonymous>
     *
     *     visibility:local
     *
     *     modality:FINAL <> (dto:READ_DTO of com.lovelycatv.crystalframework.shared.service.BaseManagerService, limit:kotlin.Int, offset:kotlin.Int)
     *
     *     returnType:com.lovelycatv.crystalframework.shared.request.PaginatedResponseData<ENTITY of com.lovelycatv.crystalframework.shared.service.BaseManagerService> [suspend]
     *
     *     in file BaseManagerService.kt has no continuation;
     *
     *     can't call FUN IR_EXTERNAL_DECLARATION_STUB
     *
     *     name:awaitFirstOrNull
     *
     *     visibility:public
     *
     *     modality:FINAL <T> (<this>:org.reactivestreams.Publisher<T of kotlinx.coroutines.reactive.AwaitKt.awaitFirstOrNull>)
     *
     *     returnType:T of kotlinx.coroutines.reactive.AwaitKt.awaitFirstOrNull? [suspend]
     *
     * @param dto
     * @param limit
     * @param offset
     * @return
     */
    private suspend fun defaultAdvanceQuery(
        dto: READ_DTO,
        limit: Int,
        offset: Int
    ): PaginatedResponseData<ENTITY> {
        val keyword = dto.searchKeyword ?: return PaginatedResponseData(
            page = dto.page,
            pageSize = dto.pageSize,
            total = 0,
            totalPages = 0,
            records = emptyList()
        )

        val total = this.getRepository().countByKeyword(keyword).awaitFirstOrNull() ?: 0
        val records = this.getRepository().searchByKeyword(
            keyword,
            limit,
            offset
        ).awaitListWithTimeout()

        return dto.toPaginatedResponseData(
            total = total,
            records = records
        )
    }

    suspend fun create(dto: CREATE_DTO): ENTITY

    suspend fun update(dto: UPDATE_DTO): ENTITY? {
        val existing = this.getByIdOrNull(dto.id) ?: return null

        val result = this.getRepository().save(
            this.applyDTOToEntity(dto, existing).apply {
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
