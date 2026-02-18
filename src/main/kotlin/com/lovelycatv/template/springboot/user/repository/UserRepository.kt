package com.lovelycatv.template.springboot.user.repository

import com.lovelycatv.template.springboot.user.entity.UserEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserRepository : R2dbcRepository<UserEntity, Long> {
    fun findByUsernameOrEmail(
        username: String,
        email: String
    ): Mono<UserEntity>

    fun findByEmail(email: String): Mono<UserEntity>
}