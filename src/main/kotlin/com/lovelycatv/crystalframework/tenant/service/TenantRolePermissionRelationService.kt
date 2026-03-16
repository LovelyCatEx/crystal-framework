package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.tenant.entity.TenantRolePermissionRelationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRolePermissionRelationRepository

interface TenantRolePermissionRelationService : CachedBaseService<TenantRolePermissionRelationRepository, TenantRolePermissionRelationEntity> {
    suspend fun getRolePermissions(roleId: Long): List<TenantPermissionEntity>

    suspend fun setRolePermissions(roleId: Long, permissionIds: List<Long>)

    suspend fun deleteByPermissionIdIn(permissionIds: Collection<Long>)

    suspend fun deleteByRoleIdIn(roleIds: Collection<Long>)
}
