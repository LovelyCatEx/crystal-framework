package com.lovelycatv.crystalframework.audit.repository

import com.lovelycatv.crystalframework.audit.entity.AuditLogEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface AuditLogRepository : BaseRepository<AuditLogEntity> {
    @Query(
        """
        SELECT * FROM audit_manager_logs
        WHERE (:#{#keyword == null} = true
           OR (LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(path) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(remote_ip) LIKE LOWER(CONCAT('%', :keyword, '%'))))
        AND (:#{#userId == null} = true OR user_id = :userId)
        AND (:#{#username == null} = true OR LOWER(username) LIKE LOWER(CONCAT('%', :username, '%')))
        AND (:#{#action == null} = true OR action = :action)
        AND (:#{#path == null} = true OR LOWER(path) LIKE LOWER(CONCAT('%', :path, '%')))
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
        action: Int?,
        path: String?,
        remoteIp: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int
    ): Flux<AuditLogEntity>

    @Query(
        """
        SELECT COUNT(*) FROM audit_manager_logs
        WHERE (:#{#keyword == null} = true
           OR (LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(path) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(remote_ip) LIKE LOWER(CONCAT('%', :keyword, '%'))))
        AND (:#{#userId == null} = true OR user_id = :userId)
        AND (:#{#username == null} = true OR LOWER(username) LIKE LOWER(CONCAT('%', :username, '%')))
        AND (:#{#action == null} = true OR action = :action)
        AND (:#{#path == null} = true OR LOWER(path) LIKE LOWER(CONCAT('%', :path, '%')))
        AND (:#{#remoteIp == null} = true OR LOWER(remote_ip) LIKE LOWER(CONCAT('%', :remoteIp, '%')))
        AND (:#{#startTime == null} = true OR created_time >= :startTime)
        AND (:#{#endTime == null} = true OR created_time <= :endTime)
    """
    )
    fun countAdvanceSearch(
        keyword: String?,
        userId: Long?,
        username: String?,
        action: Int?,
        path: String?,
        remoteIp: String?,
        startTime: Long?,
        endTime: Long?
    ): Mono<Long>

    @Query(
        """
        SELECT * FROM audit_manager_logs 
        WHERE LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(path) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(remote_ip) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    override fun searchByKeyword(
        keyword: String,
        limit: Int,
        offset: Int
    ): Flux<AuditLogEntity>

    @Query(
        """
        SELECT COUNT(*) FROM audit_manager_logs 
        WHERE LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(path) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(remote_ip) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """
    )
    override fun countByKeyword(
        keyword: String
    ): Mono<Long>
}
