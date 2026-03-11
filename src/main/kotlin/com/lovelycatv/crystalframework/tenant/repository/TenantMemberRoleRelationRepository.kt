package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberRoleRelationEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantMemberRoleRelationRepository : BaseRepository<TenantMemberRoleRelationEntity> {
    fun findAllByMemberId(memberId: Long): Flux<TenantMemberRoleRelationEntity>

    fun findAllByRoleId(roleId: Long): Flux<TenantMemberRoleRelationEntity>

    fun findByMemberIdAndRoleId(memberId: Long, roleId: Long): Mono<TenantMemberRoleRelationEntity>

    @Query(
        """
        SELECT * FROM tenant_member_role_relations 
        WHERE (:#{#memberId == null} = true OR member_id = :memberId)
        AND (:#{#roleId == null} = true OR role_id = :roleId)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        @Param("memberId") memberId: Long?,
        @Param("roleId") roleId: Long?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flux<TenantMemberRoleRelationEntity>

    @Query(
        """
        SELECT COUNT(*) FROM tenant_member_role_relations 
        WHERE (:#{#memberId == null} = true OR member_id = :memberId)
        AND (:#{#roleId == null} = true OR role_id = :roleId)
    """
    )
    fun countAdvanceSearch(
        @Param("memberId") memberId: Long?,
        @Param("roleId") roleId: Long?
    ): Mono<Long>
}
