package com.lovelycatv.template.springboot.rbac.repository

import com.lovelycatv.template.springboot.rbac.entity.UserRoleRelationEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface UserRoleRelationRepository : ReactiveCrudRepository<UserRoleRelationEntity, Long> {
    fun findByUserId(userId: Long): Flux<UserRoleRelationEntity>
}