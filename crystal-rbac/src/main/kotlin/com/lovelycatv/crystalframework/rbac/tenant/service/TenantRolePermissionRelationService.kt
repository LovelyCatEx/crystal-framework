package com.lovelycatv.crystalframework.rbac.tenant.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantRolePermissionRelationEntity
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantRolePermissionRelationRepository

interface TenantRolePermissionRelationService : CachedBaseService<TenantRolePermissionRelationRepository, TenantRolePermissionRelationEntity> {
    suspend fun getRolePermissions(roleId: Long): List<TenantPermissionEntity>

    suspend fun getRolePermissions(roleIds: List<Long>): List<TenantPermissionEntity>

    suspend fun setRolePermissions(roleId: Long, permissionIds: List<Long>)

    suspend fun deleteByPermissionIdIn(permissionIds: Collection<Long>)

    suspend fun deleteByRoleIdIn(roleIds: Collection<Long>)
}
