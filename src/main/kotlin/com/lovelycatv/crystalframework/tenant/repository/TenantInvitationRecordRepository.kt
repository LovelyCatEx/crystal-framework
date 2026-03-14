package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationRecordEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantInvitationRecordRepository : BaseRepository<TenantInvitationRecordEntity> {
    fun findAllByInvitationId(invitationId: Long): Flux<TenantInvitationRecordEntity>

    fun findAllByUsedUserId(usedUserId: Long): Flux<TenantInvitationRecordEntity>

    fun findByInvitationIdAndUsedUserId(invitationId: Long, usedUserId: Long): Mono<TenantInvitationRecordEntity>

    @Query(
        """
        SELECT * FROM tenant_invitation_records
        WHERE (:#{#keyword == null} = true 
            OR CAST(id AS TEXT) = :keyword 
            OR CAST(invitation_id AS TEXT) = :keyword
            OR CAST(used_user_id AS TEXT) = :keyword)
        AND invitation_id = :invitationId
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        @Param("keyword") keyword: String?,
        @Param("invitationId") invitationId: Long,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flux<TenantInvitationRecordEntity>

    @Query(
        """
        SELECT COUNT(*) FROM tenant_invitation_records
        WHERE (:#{#keyword == null} = true 
            OR CAST(id AS TEXT) = :keyword 
            OR CAST(invitation_id AS TEXT) = :keyword
            OR CAST(used_user_id AS TEXT) = :keyword)
        AND invitation_id = :invitationId
    """
    )
    fun countAdvanceSearch(
        @Param("keyword") keyword: String?,
        @Param("invitationId") invitationId: Long
    ): Mono<Long>

    fun countByInvitationId(invitationId: Long): Mono<Long>
}