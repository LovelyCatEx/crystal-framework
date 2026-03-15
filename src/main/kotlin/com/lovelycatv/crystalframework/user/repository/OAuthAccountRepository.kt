package com.lovelycatv.crystalframework.user.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface OAuthAccountRepository : BaseRepository<OAuthAccountEntity> {
    @Query(
        """
        SELECT * FROM oauth_accounts 
        WHERE (LOWER(identifier) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    override fun searchByKeyword(
        keyword: String,
        limit: Int,
        offset: Int
    ): Flux<OAuthAccountEntity>

    @Query(
        """
        SELECT COUNT(*) FROM oauth_accounts 
        WHERE LOWER(identifier) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """
    )
    override fun countByKeyword(
        keyword: String
    ): Mono<Long>

    @Query(
        """
        SELECT * FROM oauth_accounts 
        WHERE (:#{#keyword == null} = true
           OR (LOWER(identifier) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))))
        AND (:#{#platform == null} = true OR platform = :platform)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        keyword: String?,
        platform: Int?,
        limit: Int,
        offset: Int
    ): Flux<OAuthAccountEntity>

    @Query(
        """
        SELECT COUNT(*) FROM oauth_accounts 
        WHERE (:#{#keyword == null} = true
           OR (LOWER(identifier) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))))
        AND (:#{#platform == null} = true OR platform = :platform)
    """
    )
    fun countAdvanceSearch(
        keyword: String?,
        platform: Int?,
    ): Mono<Long>

    fun findByPlatformAndIdentifier(platform: Int, identifier: String): Mono<OAuthAccountEntity>

    fun findAllByUserId(userId: Long): Flux<OAuthAccountEntity>

    fun findByPlatformAndUserId(platform: Int, userId: Long): Mono<OAuthAccountEntity>

    @Query("SELECT COUNT(*) FROM oauth_accounts WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}