package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantRoleEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantRoleRepository : BaseRepository<TenantRoleEntity> {
    fun findAllByTenantId(tenantId: Long): Flux<TenantRoleEntity>

    fun findAllByTenantIdAndParentId(tenantId: Long, parentId: Long?): Flux<TenantRoleEntity>

    fun findByTenantIdAndName(tenantId: Long, name: String): Mono<TenantRoleEntity>

    @Query(
        """
        SELECT * FROM tenantroles 
        WHERE (:#{#keyword == null} = true OR LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:#{#tenantId == null} = true OR tenant_id = :tenantId)
        AND (:#{#parentId == null} = true OR parent_id = :parentId)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        @Param("keyword") keyword: String?,
        @Param("tenantId") tenantId: Long?,
        @Param("parentId") parentId: Long?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flux<TenantRoleEntity>

    @Query(
        """
        SELECT COUNT(*) FROM tenantroles 
        WHERE (:#{#keyword == null} = true OR LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:#{#tenantId == null} = true OR tenant_id = :tenantId)
        AND (:#{#parentId == null} = true OR parent_id = :parentId)
    """
    )
    fun countAdvanceSearch(
        @Param("keyword") keyword: String?,
        @Param("tenantId") tenantId: Long?,
        @Param("parentId") parentId: Long?
    ): Mono<Long>
}
