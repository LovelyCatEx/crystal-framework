package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.entity.ScopedEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.service.TenantRelationshipCheckService

interface BaseTenantResourceManagerService<
        REPOSITORY: BaseRepository<ENTITY>,
        ENTITY,
        CREATE_DTO: Any,
        READ_DTO: BaseManagerReadDTO,
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

    suspend fun findAllByTenantId(tenantId: Long): List<ENTITY>
}