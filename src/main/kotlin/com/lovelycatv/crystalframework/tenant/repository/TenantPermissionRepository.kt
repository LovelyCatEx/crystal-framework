package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantPermissionEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantPermissionRepository : BaseRepository<TenantPermissionEntity> {
    fun findByName(name: String): Mono<TenantPermissionEntity>

    @Query(
        """
        SELECT * FROM tenant_permissions 
        WHERE (LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:#{#type == null} = true OR type = :type)
        AND (:#{#startTime == null} = true OR created_time >= :startTime)
        AND (:#{#endTime == null} = true OR created_time <= :endTime)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        @Param("keyword") keyword: String?,
        @Param("type") type: Int?,
        @Param("startTime") startTime: Long?,
        @Param("endTime") endTime: Long?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flux<TenantPermissionEntity>

    @Query(
        """
        SELECT COUNT(*) FROM tenant_permissions 
        WHERE (LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:#{#type == null} = true OR type = :type)
        AND (:#{#startTime == null} = true OR created_time >= :startTime)
        AND (:#{#endTime == null} = true OR created_time <= :endTime)
    """
    )
    fun countAdvanceSearch(
        @Param("keyword") keyword: String?,
        @Param("type") type: Int?,
        @Param("startTime") startTime: Long?,
        @Param("endTime") endTime: Long?,
    ): Mono<Long>

    fun findByNameIn(names: Collection<String>): Flux<TenantPermissionEntity>
}
