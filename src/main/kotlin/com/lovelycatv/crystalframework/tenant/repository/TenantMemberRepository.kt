package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantMemberRepository : BaseRepository<TenantMemberEntity> {
    fun findAllByTenantId(tenantId: Long): Flux<TenantMemberEntity>

    fun findAllByMemberUserId(memberUserId: Long): Flux<TenantMemberEntity>

    fun findByTenantIdAndMemberUserId(tenantId: Long, memberUserId: Long): Mono<TenantMemberEntity>

    @Query(
        """
        SELECT * FROM tenant_members
        WHERE (:#{#keyword == null} = true 
            OR CAST(id AS TEXT) = :keyword 
            OR CAST(member_user_id AS TEXT) = :keyword
            OR member_user_id IN (
                SELECT id FROM users 
                WHERE LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ))
        AND tenant_id = :tenantId
        AND (:#{#status == null} = true OR status = :status)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        @Param("keyword") keyword: String?,
        @Param("tenantId") tenantId: Long,
        @Param("status") status: Int?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flux<TenantMemberEntity>

    @Query(
        """
        SELECT COUNT(*) FROM tenant_members
        WHERE (:#{#keyword == null} = true 
            OR CAST(id AS TEXT) = :keyword 
            OR CAST(member_user_id AS TEXT) = :keyword
            OR member_user_id IN (
                SELECT id FROM users 
                WHERE LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ))
        AND tenant_id = :tenantId
        AND (:#{#status == null} = true OR status = :status)
    """
    )
    fun countAdvanceSearch(
        @Param("keyword") keyword: String?,
        @Param("tenantId") tenantId: Long,
        @Param("status") status: Int?
    ): Mono<Long>

    @Query("SELECT COUNT(*) FROM tenant_members WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}
