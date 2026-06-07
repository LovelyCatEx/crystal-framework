package com.lovelycatv.crystalframework.user.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface OAuthAccountRepository : BaseRepository<OAuthAccountEntity> {
    /**
     * All non-deleted binding rows for a third-party identity (one per scope/tenant). Callers
     * filter by scope/tenantId in code — Spring Data derived queries bind a null tenant_id as
     * `= NULL`, which never matches SYSTEM-scope rows, so we avoid them here.
     */
    fun findAllByPlatformAndIdentifier(platform: Int, identifier: String): Flux<OAuthAccountEntity>

    fun findAllByUserId(userId: Long): Flux<OAuthAccountEntity>

    fun findAllByUserIdAndScopeAndTenantId(userId: Long, scope: Int, tenantId: Long): Flux<OAuthAccountEntity>

    @Query("SELECT COUNT(*) FROM oauth_accounts WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}