package com.lovelycatv.crystalframework.auth.repository

import com.lovelycatv.crystalframework.auth.entity.UserLoginLogEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserLoginLogRepository : BaseRepository<UserLoginLogEntity> {
    @Query(
        """
        SELECT * FROM user_login_logs
        WHERE deleted_time IS NULL
          AND (:#{#keyword == null} = true
             OR (LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(oauth2_username) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(remote_ip) LIKE LOWER(CONCAT('%', :keyword, '%'))))
          AND (:#{#userId == null} = true OR user_id = :userId)
          AND (:#{#username == null} = true OR LOWER(username) LIKE LOWER(CONCAT('%', :username, '%')))
          AND (:#{#tenantId == null} = true OR tenant_id = :tenantId)
          AND (:#{#loginMethod == null} = true OR login_method = :loginMethod)
          AND (:#{#oauth2Type == null} = true OR oauth2_type = :oauth2Type)
          AND (:#{#success == null} = true OR success = :success)
          AND (:#{#remoteIp == null} = true OR LOWER(remote_ip) LIKE LOWER(CONCAT('%', :remoteIp, '%')))
          AND (:#{#startTime == null} = true OR created_time >= :startTime)
          AND (:#{#endTime == null} = true OR created_time <= :endTime)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
        """
    )
    fun advanceSearch(
        keyword: String?,
        userId: Long?,
        username: String?,
        tenantId: Long?,
        loginMethod: Int?,
        oauth2Type: Int?,
        success: Boolean?,
        remoteIp: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int
    ): Flux<UserLoginLogEntity>

    @Query(
        """
        SELECT COUNT(*) FROM user_login_logs
        WHERE deleted_time IS NULL
          AND (:#{#keyword == null} = true
             OR (LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(oauth2_username) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(remote_ip) LIKE LOWER(CONCAT('%', :keyword, '%'))))
          AND (:#{#userId == null} = true OR user_id = :userId)
          AND (:#{#username == null} = true OR LOWER(username) LIKE LOWER(CONCAT('%', :username, '%')))
          AND (:#{#tenantId == null} = true OR tenant_id = :tenantId)
          AND (:#{#loginMethod == null} = true OR login_method = :loginMethod)
          AND (:#{#oauth2Type == null} = true OR oauth2_type = :oauth2Type)
          AND (:#{#success == null} = true OR success = :success)
          AND (:#{#remoteIp == null} = true OR LOWER(remote_ip) LIKE LOWER(CONCAT('%', :remoteIp, '%')))
          AND (:#{#startTime == null} = true OR created_time >= :startTime)
          AND (:#{#endTime == null} = true OR created_time <= :endTime)
        """
    )
    fun countAdvanceSearch(
        keyword: String?,
        userId: Long?,
        username: String?,
        tenantId: Long?,
        loginMethod: Int?,
        oauth2Type: Int?,
        success: Boolean?,
        remoteIp: String?,
        startTime: Long?,
        endTime: Long?
    ): Mono<Long>
}