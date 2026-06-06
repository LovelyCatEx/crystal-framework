package com.lovelycatv.crystalframework.user.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.user.entity.UserEntity
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserRepository : BaseRepository<UserEntity> {
    fun findByUsernameOrEmail(
        username: String,
        email: String
    ): Mono<UserEntity>

    fun findByEmail(email: String): Mono<UserEntity>

    fun findByUsername(username: String): Mono<UserEntity>

    @Modifying
    @Query("UPDATE users SET avatar = :avatar WHERE id = :id")
    fun updateAvatar(id: Long, avatar: Long): Mono<Long>

    @Query("SELECT COUNT(*) FROM users WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}
