package com.lovelycatv.template.springboot.rbac.repository

import com.lovelycatv.template.springboot.rbac.entity.UserRoleRelationEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface UserRoleRelationRepository : R2dbcRepository<UserRoleRelationEntity, Long> {
    @Query("SELECT * FROM user_role_relations WHERE user_id = :userId")
    fun findByUserId(userId: Long): Flux<UserRoleRelationEntity>
}