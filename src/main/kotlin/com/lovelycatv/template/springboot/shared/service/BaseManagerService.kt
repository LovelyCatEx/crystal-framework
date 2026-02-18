package com.lovelycatv.template.springboot.shared.service

import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.template.springboot.shared.entity.BaseEntity
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import com.lovelycatv.template.springboot.shared.request.PaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface BaseManagerService<
        REPOSITORY: R2dbcRepository<ENTITY, Long>,
        ENTITY: BaseEntity,
        CREATE_DTO: Any,
        READ_DTO: BaseManagerReadDTO,
        UPDATE_DTO: BaseManagerUpdateDTO,
        DELETE_DTO: BaseManagerDeleteDTO
> : BaseService<REPOSITORY, ENTITY> {
    suspend fun query(baseManagerReadDTO: READ_DTO): PaginatedResponseData<ENTITY>

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
                ?: throw BusinessException("Could not delete resource, unexpected returns value")
        } catch (_: Exception) {
            throw BusinessException("Could not delete resource")
        }
    }

    suspend fun deleteByDTO(dto: DELETE_DTO) {
        this.delete(dto.id)
    }
}