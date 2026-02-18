package com.lovelycatv.template.springboot.user.repository

import com.lovelycatv.template.springboot.user.entity.UserEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserRepository : ReactiveCrudRepository<UserEntity, Long> {
    fun findByUsernameOrEmail(
        username: String,
        email: String
    ): Mono<UserEntity>

    fun findByEmail(email: String): Mono<UserEntity>
}