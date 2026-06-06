package com.lovelycatv.crystalframework.rbac.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantMemberRoleRelationEntity
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantMemberRoleRelationRepository

interface TenantMemberRoleRelationService : CachedBaseService<TenantMemberRoleRelationRepository, TenantMemberRoleRelationEntity> {
    suspend fun getMemberRoles(memberId: Long): List<TenantRoleEntity>

    suspend fun getMemberRolesRecursive(memberId: Long): Set<TenantRoleEntity>

    suspend fun setMemberRoles(memberId: Long, roleIds: List<Long>)

    suspend fun deleteByRoleIdIn(roleIds: Collection<Long>)

    suspend fun deleteByMemberIdIn(memberIds: Collection<Long>)
}
