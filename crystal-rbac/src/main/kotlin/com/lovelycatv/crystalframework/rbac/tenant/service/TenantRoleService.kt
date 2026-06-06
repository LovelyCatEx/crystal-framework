package com.lovelycatv.crystalframework.rbac.tenant.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantRoleRepository

interface TenantRoleService : CachedBaseService<TenantRoleRepository, TenantRoleEntity> {
    suspend fun getChildren(roleId: Long): List<TenantRoleEntity>

    suspend fun getParents(roleId: Long): List<TenantRoleEntity>
}
