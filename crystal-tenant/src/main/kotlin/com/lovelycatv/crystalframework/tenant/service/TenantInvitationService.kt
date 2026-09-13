/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRepository
import org.springframework.transaction.annotation.Transactional

interface TenantInvitationService : CachedBaseService<TenantInvitationRepository, TenantInvitationEntity> {
    suspend fun getInvitationByCode(code: String): TenantInvitationEntity

    suspend fun isOverInvitationCount(invitationCode: String): Boolean {
        return this.isOverInvitationCount(
            this.getInvitationByCode(invitationCode)
        )
    }

    suspend fun isOverInvitationCount(invitation: TenantInvitationEntity): Boolean

    @Transactional(rollbackFor = [Exception::class])
    suspend fun acceptInvitation(userId: Long, invitationCode: String, realName: String, phoneNumber: String)
}