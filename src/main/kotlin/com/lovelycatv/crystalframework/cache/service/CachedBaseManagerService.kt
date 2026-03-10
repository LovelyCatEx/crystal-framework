package com.lovelycatv.crystalframework.cache.service

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.shared.service.BaseManagerService

interface CachedBaseManagerService<
        REPOSITORY: BaseRepository<ENTITY>,
        ENTITY: BaseEntity,
        CREATE_DTO: Any,
        READ_DTO: BaseManagerReadDTO,
        UPDATE_DTO: BaseManagerUpdateDTO,
        DELETE_DTO: BaseManagerDeleteDTO
> : BaseManagerService<REPOSITORY, ENTITY, CREATE_DTO, READ_DTO, UPDATE_DTO, DELETE_DTO>,
    CachedBaseService<REPOSITORY, ENTITY> {
    override suspend fun update(dto: UPDATE_DTO): ENTITY? {
        return super.withUpdateEntityContext(dto.id) {
            super.update(dto)
        }
    }

    override suspend fun batchDelete(ids: List<Long>) {
        super.withBatchDeleteEntityContext(ids) {
            super.batchDelete(ids)
        }
    }
}