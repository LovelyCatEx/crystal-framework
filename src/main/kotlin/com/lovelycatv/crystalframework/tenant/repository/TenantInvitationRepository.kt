package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantInvitationRepository : BaseRepository<TenantInvitationEntity> {
    fun findAllByTenantId(tenantId: Long): Flux<TenantInvitationEntity>

    fun findAllByCreatorMemberId(creatorMemberId: Long): Flux<TenantInvitationEntity>

    fun findAllByDepartmentId(departmentId: Long): Flux<TenantInvitationEntity>

    fun findByInvitationCode(invitationCode: String): Mono<TenantInvitationEntity>

    fun findByTenantIdAndInvitationCode(tenantId: Long, invitationCode: String): Mono<TenantInvitationEntity>

    @Query(
        """
        SELECT * FROM tenant_invitations
        WHERE (:#{#keyword == null} = true 
            OR CAST(id AS TEXT) = :keyword 
            OR invitation_code = :keyword)
        AND tenant_id = :tenantId
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        @Param("keyword") keyword: String?,
        @Param("tenantId") tenantId: Long,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flux<TenantInvitationEntity>

    @Query(
        """
        SELECT COUNT(*) FROM tenant_invitations
        WHERE (:#{#keyword == null} = true 
            OR CAST(id AS TEXT) = :keyword 
            OR invitation_code = :keyword)
        AND tenant_id = :tenantId
    """
    )
    fun countAdvanceSearch(
        @Param("keyword") keyword: String?,
        @Param("tenantId") tenantId: Long
    ): Mono<Long>

    fun getByInvitationCode(invitationCode: String): Mono<TenantInvitationEntity>
}