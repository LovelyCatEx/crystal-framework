package com.lovelycatv.crystalframework.user.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface OAuthAccountRepository : BaseRepository<OAuthAccountEntity> {
    fun findByPlatformAndIdentifier(platform: Int, identifier: String): Mono<OAuthAccountEntity>

    fun findAllByUserId(userId: Long): Flux<OAuthAccountEntity>

    fun findByPlatformAndUserId(platform: Int, userId: Long): Mono<OAuthAccountEntity>

    @Query("SELECT COUNT(*) FROM oauth_accounts WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}
