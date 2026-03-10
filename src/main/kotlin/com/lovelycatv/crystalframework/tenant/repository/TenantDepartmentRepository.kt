package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantDepartmentRepository : BaseRepository<TenantDepartmentEntity> {
    fun findAllByTenantId(tenantId: Long): Flux<TenantDepartmentEntity>

    fun findAllByTenantIdAndParentId(tenantId: Long, parentId: Long?): Flux<TenantDepartmentEntity>

    @Query(
        """
        SELECT * FROM tenantdepartments 
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
    ): Flux<TenantDepartmentEntity>

    @Query(
        """
        SELECT COUNT(*) FROM tenantdepartments 
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
