package com.lovelycatv.crystalframework.rbac.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.ManagerCreateTenantPermissionDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.ManagerDeleteTenantPermissionDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.ManagerReadTenantPermissionDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.ManagerUpdateTenantPermissionDTO
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantPermissionRepository

interface TenantPermissionManagerService : CachedBaseManagerService<
        TenantPermissionRepository,
        TenantPermissionEntity,
        ManagerCreateTenantPermissionDTO,
        ManagerReadTenantPermissionDTO,
        ManagerUpdateTenantPermissionDTO,
        ManagerDeleteTenantPermissionDTO
>
