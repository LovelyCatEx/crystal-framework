package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantRolePermissionRelationEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantRolePermissionRelationRepository : BaseRepository<TenantRolePermissionRelationEntity> {
    fun findAllByRoleId(roleId: Long): Flux<TenantRolePermissionRelationEntity>

    fun findAllByRoleIdIn(roleIds: Collection<Long>): Flux<TenantRolePermissionRelationEntity>

    fun findAllByPermissionId(permissionId: Long): Flux<TenantRolePermissionRelationEntity>

    fun findAllByPermissionIdIn(permissionIds: Collection<Long>): Flux<TenantRolePermissionRelationEntity>

    fun findByRoleIdAndPermissionId(roleId: Long, permissionId: Long): Mono<TenantRolePermissionRelationEntity>

    fun deleteByPermissionIdIn(permissionIds: Collection<Long>): Mono<Void>

    fun deleteByRoleIdIn(roleIds: Collection<Long>): Mono<Void>
}
