package com.lovelycatv.crystalframework.rbac.repository

import com.lovelycatv.crystalframework.rbac.entity.UserRolePermissionRelationEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserRolePermissionRelationRepository : BaseRepository<UserRolePermissionRelationEntity> {
    fun findByRoleId(roleId: Long): Flux<UserRolePermissionRelationEntity>

    fun findByPermissionIdIn(permissionIds: Collection<Long>): Flux<UserRolePermissionRelationEntity>

    fun deleteByPermissionIdIn(permissionIds: Collection<Long>): Mono<Void>

    fun deleteByRoleIdIn(roleIds: Collection<Long>): Mono<Void>
}