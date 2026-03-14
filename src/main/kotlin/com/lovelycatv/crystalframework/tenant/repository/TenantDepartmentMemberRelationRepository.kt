package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantDepartmentMemberRelationRepository : BaseRepository<TenantDepartmentMemberRelationEntity> {
    fun findAllByDepartmentId(departmentId: Long): Flux<TenantDepartmentMemberRelationEntity>

    fun findAllByMemberId(memberId: Long): Flux<TenantDepartmentMemberRelationEntity>

    fun findByDepartmentIdAndMemberId(departmentId: Long, memberId: Long): Mono<TenantDepartmentMemberRelationEntity>

    @Query(
        """
        SELECT * FROM tenant_department_member_relations 
        WHERE (:#{#departmentId == null} = true OR department_id = :departmentId)
        AND (:#{#memberId == null} = true OR member_id = :memberId)
        AND (:#{#roleType == null} = true OR role_type = :roleType)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        @Param("departmentId") departmentId: Long?,
        @Param("memberId") memberId: Long?,
        @Param("roleType") roleType: Int?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int
    ): Flux<TenantDepartmentMemberRelationEntity>

    @Query(
        """
        SELECT COUNT(*) FROM tenant_department_member_relations 
        WHERE (:#{#departmentId == null} = true OR department_id = :departmentId)
        AND (:#{#memberId == null} = true OR member_id = :memberId)
        AND (:#{#roleType == null} = true OR role_type = :roleType)
    """
    )
    fun countAdvanceSearch(
        @Param("departmentId") departmentId: Long?,
        @Param("memberId") memberId: Long?,
        @Param("roleType") roleType: Int?
    ): Mono<Long>

    fun deleteByMemberIdIn(memberIds: Collection<Long>): Mono<Void>
}
