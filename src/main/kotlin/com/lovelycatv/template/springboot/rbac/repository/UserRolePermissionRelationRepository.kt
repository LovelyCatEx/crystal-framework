package com.lovelycatv.template.springboot.rbac.repository

import com.lovelycatv.template.springboot.rbac.entity.UserRolePermissionRelationEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface UserRolePermissionRelationRepository : ReactiveCrudRepository<UserRolePermissionRelationEntity, Long> {
    fun findByRoleId(roleId: Long): Flux<UserRolePermissionRelationEntity>
}