package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantRolePermissionRelationEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantRolePermissionRelationRepository : BaseRepository<TenantRolePermissionRelationEntity> {
    fun findAllByRoleId(roleId: Long): Flux<TenantRolePermissionRelationEntity>

    fun findAllByPermissionId(permissionId: Long): Flux<TenantRolePermissionRelationEntity>

    fun findByRoleIdAndPermissionId(roleId: Long, permissionId: Long): Mono<TenantRolePermissionRelationEntity>

    @Query(
        """
        SELECT * FROM tenantrole_permission_relations 
        WHERE (:#{#roleId == null} = true OR role_id = :roleId)
        AND (:#{#permissionId == null} = true OR permission_id = :permissionId)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        @Param("roleId") roleId: Long?,
        @Param("permissionId") permissionId: Long?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flux<TenantRolePermissionRelationEntity>

    @Query(
        """
        SELECT COUNT(*) FROM tenantrole_permission_relations 
        WHERE (:#{#roleId == null} = true OR role_id = :roleId)
        AND (:#{#permissionId == null} = true OR permission_id = :permissionId)
    """
    )
    fun countAdvanceSearch(
        @Param("roleId") roleId: Long?,
        @Param("permissionId") permissionId: Long?
    ): Mono<Long>
}
