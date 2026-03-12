package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantRepository : BaseRepository<TenantEntity> {
    fun findByName(name: String): Mono<TenantEntity>

    fun findAllByOwnerUserId(ownerUserId: Long): Flux<TenantEntity>

    @Query(
        """
        SELECT * FROM tenants 
        WHERE (:#{#keyword == null} = true OR LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:#{#status == null} = true OR status = :status)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        @Param("keyword") keyword: String?,
        @Param("status") status: Int?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flux<TenantEntity>

    @Query(
        """
        SELECT COUNT(*) FROM tenants 
        WHERE (:#{#keyword == null} = true OR LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:#{#status == null} = true OR status = :status)
    """
    )
    fun countAdvanceSearch(
        @Param("keyword") keyword: String?,
        @Param("status") status: Int?
    ): Mono<Long>
}
