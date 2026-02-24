package com.lovelycatv.template.springboot.shared.service

import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.template.springboot.shared.entity.BaseEntity
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.repository.BaseRepository
import com.lovelycatv.template.springboot.shared.request.PaginatedResponseData
import com.lovelycatv.template.springboot.shared.utils.awaitListWithTimeout
import com.lovelycatv.template.springboot.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface BaseManagerService<
        REPOSITORY: BaseRepository<ENTITY>,
        ENTITY: BaseEntity,
        CREATE_DTO: Any,
        READ_DTO: BaseManagerReadDTO,
        UPDATE_DTO: BaseManagerUpdateDTO,
        DELETE_DTO: BaseManagerDeleteDTO
> : BaseService<REPOSITORY, ENTITY> {
    suspend fun query(dto: READ_DTO): PaginatedResponseData<ENTITY> {
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

            if (dto.searchKeyword != null) {
                // Search by keywords
                val keyword = dto.searchKeyword!!

                val total = this.getRepository().countByKeyword(keyword).awaitFirstOrNull() ?: 0
                val records = this.getRepository().searchByKeyword(
                    keyword,
                    limit,
                    offset
                ).awaitListWithTimeout()

                dto.toPaginatedResponseData(
                    total = total,
                    records = records
                )
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

    suspend fun delete(id: Long) {
        try {
            this.getRepository().deleteById(id).awaitFirstOrNull()
        } catch (_: Exception) {
            throw BusinessException("Could not delete resource")
        }
    }

    suspend fun deleteByDTO(dto: DELETE_DTO) {
        this.delete(dto.id)
    }
}