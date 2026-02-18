package com.lovelycatv.template.springboot.rbac.repository

import com.lovelycatv.template.springboot.rbac.entity.UserRoleRelationEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserRoleRelationRepository : ReactiveCrudRepository<UserRoleRelationEntity, Long> {
    @Query("SELECT * FROM user_role_relations WHERE user_id = :userId")
    fun findByUserId(userId: Long): Flux<UserRoleRelationEntity>
}