package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.BaseTenantResourceManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerCreateTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerDeleteTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerReadTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerUpdateTenantDictItemDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.vo.TenantDictItemTreeVO
import com.lovelycatv.crystalframework.tenant.entity.TenantDictItemEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDictItemRepository

interface TenantDictItemManagerService : BaseTenantResourceManagerService<
        TenantDictItemRepository,
        TenantDictItemEntity,
        ManagerCreateTenantDictItemDTO,
        ManagerReadTenantDictItemDTO,
        ManagerUpdateTenantDictItemDTO,
        ManagerDeleteTenantDictItemDTO
        > {
    suspend fun getTreeByTypeId(typeId: Long): List<TenantDictItemTreeVO>
}
