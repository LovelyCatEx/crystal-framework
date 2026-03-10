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
        SELECT * FROM tenantmembers 
        WHERE (:#{#keyword == null} = true OR CAST(tenant_id AS TEXT) LIKE CONCAT('%', :keyword, '%'))
        AND (:#{#tenantId == null} = true OR tenant_id = :tenantId)
        AND (:#{#status == null} = true OR status = :status)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        @Param("keyword") keyword: String?,
        @Param("tenantId") tenantId: Long?,
        @Param("status") status: Int?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flux<TenantMemberEntity>

    @Query(
        """
        SELECT COUNT(*) FROM tenantmembers 
        WHERE (:#{#keyword == null} = true OR CAST(tenant_id AS TEXT) LIKE CONCAT('%', :keyword, '%'))
        AND (:#{#tenantId == null} = true OR tenant_id = :tenantId)
        AND (:#{#status == null} = true OR status = :status)
    """
    )
    fun countAdvanceSearch(
        @Param("keyword") keyword: String?,
        @Param("tenantId") tenantId: Long?,
        @Param("status") status: Int?
    ): Mono<Long>
}
