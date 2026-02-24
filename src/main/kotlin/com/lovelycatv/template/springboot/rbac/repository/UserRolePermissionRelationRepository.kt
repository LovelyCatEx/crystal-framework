package com.lovelycatv.template.springboot.rbac.repository

import com.lovelycatv.template.springboot.rbac.entity.UserRolePermissionRelationEntity
import com.lovelycatv.template.springboot.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface UserRolePermissionRelationRepository : BaseRepository<UserRolePermissionRelationEntity> {
    fun findByRoleId(roleId: Long): Flux<UserRolePermissionRelationEntity>
}