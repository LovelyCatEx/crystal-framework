package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerCreateTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerDeleteTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerReadTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerUpdateTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantTireTypeEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireTypeRepository

interface TenantTireTypeManagerService : CachedBaseManagerService<
        TenantTireTypeRepository,
        TenantTireTypeEntity,
        ManagerCreateTenantTireTypeDTO,
        ManagerReadTenantTireTypeDTO,
        ManagerUpdateTenantTireTypeDTO,
        ManagerDeleteTenantTireTypeDTO
>
