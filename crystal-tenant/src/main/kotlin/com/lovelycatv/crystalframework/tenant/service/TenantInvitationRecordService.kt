/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationRecordEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRecordRepository

interface TenantInvitationRecordService : CachedBaseService<TenantInvitationRecordRepository, TenantInvitationRecordEntity> {
    suspend fun saveRecord(invitationId: Long, userId: Long, realName: String, phoneNumber: String): TenantInvitationRecordEntity

    suspend fun getAllByInvitationId(invitationId: Long): List<TenantInvitationRecordEntity>

    suspend fun getAllByUsedUserId(usedUserId: Long): List<TenantInvitationRecordEntity>
}
