/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationEntity
import org.springframework.data.r2dbc.repository.Query
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

    fun getByInvitationCode(invitationCode: String): Mono<TenantInvitationEntity>

    fun countByTenantId(tenantId: Long): Mono<Long>

    fun countByTenantIdAndCreatedTimeGreaterThanEqual(tenantId: Long, time: Long): Mono<Long>

    @Query("SELECT COUNT(*) FROM tenant_invitations WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}
