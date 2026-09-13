/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationRecordEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface TenantInvitationRecordRepository : BaseRepository<TenantInvitationRecordEntity> {
    fun findAllByInvitationId(invitationId: Long): Flux<TenantInvitationRecordEntity>

    fun findAllByUsedUserId(usedUserId: Long): Flux<TenantInvitationRecordEntity>

    fun findByInvitationIdAndUsedUserId(invitationId: Long, usedUserId: Long): Mono<TenantInvitationRecordEntity>

    fun countByInvitationId(invitationId: Long): Mono<Long>

    @Query("SELECT COUNT(*) FROM tenant_invitation_records WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}
