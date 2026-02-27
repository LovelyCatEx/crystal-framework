package com.lovelycatv.crystalframework.rbac.repository

import com.lovelycatv.crystalframework.rbac.entity.UserRoleRelationEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface UserRoleRelationRepository : BaseRepository<UserRoleRelationEntity> {
    @Query("SELECT * FROM user_role_relations WHERE user_id = :userId")
    fun findByUserId(userId: Long): Flux<UserRoleRelationEntity>
}