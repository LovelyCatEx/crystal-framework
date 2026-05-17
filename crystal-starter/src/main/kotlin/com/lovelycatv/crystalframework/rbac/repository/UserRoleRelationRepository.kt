package com.lovelycatv.crystalframework.rbac.repository

import com.lovelycatv.crystalframework.rbac.entity.UserRoleRelationEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserRoleRelationRepository : BaseRepository<UserRoleRelationEntity> {
    @Query("SELECT * FROM user_role_relations WHERE user_id = :userId")
    fun findByUserId(userId: Long): Flux<UserRoleRelationEntity>

    @Query("SELECT * FROM user_role_relations WHERE role_id = :roleId")
    fun findByRoleId(roleId: Long): Flux<UserRoleRelationEntity>

    fun deleteByRoleIdIn(roleIds: Collection<Long>): Mono<Void>

    fun deleteByUserIdIn(userIds: Collection<Long>): Mono<Void>
}