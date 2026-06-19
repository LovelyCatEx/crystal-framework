package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.BaseScopedManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerCreateTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerDeleteTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerReadTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerUpdateTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantDictTypeEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDictTypeRepository

interface TenantDictTypeManagerService : BaseScopedManagerService<
        TenantDictTypeRepository,
        TenantDictTypeEntity,
        ManagerCreateTenantDictTypeDTO,
        ManagerReadTenantDictTypeDTO,
        ManagerUpdateTenantDictTypeDTO,
        ManagerDeleteTenantDictTypeDTO
        >
