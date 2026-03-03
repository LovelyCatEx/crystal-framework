package com.lovelycatv.crystalframework.rbac.repository

import com.lovelycatv.crystalframework.rbac.entity.UserRolePermissionRelationEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface UserRolePermissionRelationRepository : BaseRepository<UserRolePermissionRelationEntity> {
    fun findByRoleId(roleId: Long): Flux<UserRolePermissionRelationEntity>
}