package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerCreateTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerDeleteTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerReadTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerUpdateTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantPermissionRepository

interface TenantPermissionManagerService : CachedBaseManagerService<
        TenantPermissionRepository,
        TenantPermissionEntity,
        ManagerCreateTenantPermissionDTO,
        ManagerReadTenantPermissionDTO,
        ManagerUpdateTenantPermissionDTO,
        ManagerDeleteTenantPermissionDTO
>
