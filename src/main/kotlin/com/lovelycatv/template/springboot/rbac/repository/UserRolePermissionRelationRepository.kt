package com.lovelycatv.template.springboot.rbac.repository

import com.lovelycatv.template.springboot.rbac.entity.UserRolePermissionRelationEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface UserRolePermissionRelationRepository : R2dbcRepository<UserRolePermissionRelationEntity, Long> {
    fun findByRoleId(roleId: Long): Flux<UserRolePermissionRelationEntity>
}