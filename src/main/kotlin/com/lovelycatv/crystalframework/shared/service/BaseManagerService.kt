package com.lovelycatv.crystalframework.shared.service

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface BaseManagerService<
        REPOSITORY: BaseRepository<ENTITY>,
        ENTITY: BaseEntity,
        CREATE_DTO: Any,
        READ_DTO: BaseManagerReadDTO,
        UPDATE_DTO: BaseManagerUpdateDTO,
        DELETE_DTO: BaseManagerDeleteDTO
> : BaseService<REPOSITORY, ENTITY> {
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

            if (isAdvanceQuery.invoke(dto)) {
                doAdvanceQuery.invoke(dto, limit, offset)
            } else {
                // Simple pagination
                val total = this.getRepository().count().awaitFirstOrNull() ?: 0
                val records = this.getRepository()
                    .findAllByPage(limit, offset)
                    .awaitListWithTimeout()

                dto.toPaginatedResponseData(
                    total = total,
                    records = records
                )
            }
        }
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

        return this.getRepository().save(
            this.applyDTOToEntity(dto, existing).apply {
                this.modifiedTime = System.currentTimeMillis()
            } newEntity false
        ).awaitFirstOrNull() ?: throw BusinessException("Could not update resource")
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
            this.getRepository().deleteById(id).awaitFirstOrNull()
        } catch (_: Exception) {
            throw BusinessException("Could not delete resource")
        }
    }

    suspend fun deleteByDTO(dto: DELETE_DTO) {
        this.batchDelete(dto.ids)
    }
}