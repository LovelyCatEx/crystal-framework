package com.lovelycatv.crystalframework.mail.repository

import com.lovelycatv.crystalframework.mail.entity.MailSendLogEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface MailSendLogRepository : BaseRepository<MailSendLogEntity> {
    @Query(
        """
        SELECT * FROM mail_send_logs
        WHERE (:#{#keyword == null} = true OR LOWER(to_email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(subject) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:#{#toEmail == null} = true OR to_email = :toEmail)
        AND (:#{#success == null} = true OR success = :success)
        AND (:#{#userId == null} = true OR user_id = :userId)
        AND (:#{#tenantId == null} = true OR tenant_id = :tenantId)
        AND (:#{#startTime == null} = true OR created_time >= :startTime)
        AND (:#{#endTime == null} = true OR created_time <= :endTime)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        @Param("keyword") keyword: String?,
        @Param("toEmail") toEmail: String?,
        @Param("success") success: Boolean?,
        @Param("userId") userId: Long?,
        @Param("tenantId") tenantId: Long?,
        @Param("startTime") startTime: Long?,
        @Param("endTime") endTime: Long?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flux<MailSendLogEntity>

    @Query(
        """
        SELECT COUNT(*) FROM mail_send_logs
        WHERE (:#{#keyword == null} = true OR LOWER(to_email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(subject) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:#{#toEmail == null} = true OR to_email = :toEmail)
        AND (:#{#success == null} = true OR success = :success)
        AND (:#{#userId == null} = true OR user_id = :userId)
        AND (:#{#tenantId == null} = true OR tenant_id = :tenantId)
        AND (:#{#startTime == null} = true OR created_time >= :startTime)
        AND (:#{#endTime == null} = true OR created_time <= :endTime)
    """
    )
    fun countAdvanceSearch(
        @Param("keyword") keyword: String?,
        @Param("toEmail") toEmail: String?,
        @Param("success") success: Boolean?,
        @Param("userId") userId: Long?,
        @Param("tenantId") tenantId: Long?,
        @Param("startTime") startTime: Long?,
        @Param("endTime") endTime: Long?
    ): Mono<Long>

    @Query("SELECT COUNT(*) FROM mail_send_logs WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}
