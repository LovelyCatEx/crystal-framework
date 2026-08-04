package com.lovelycatv.crystalframework.rbac.tenant.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantPermissionRepository

interface TenantPermissionService : CachedBaseService<TenantPermissionRepository, TenantPermissionEntity> {
    suspend fun getByIds(ids: Collection<Long>): List<TenantPermissionEntity>
}
