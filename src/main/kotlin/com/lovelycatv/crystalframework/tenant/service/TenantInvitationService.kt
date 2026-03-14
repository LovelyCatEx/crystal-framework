package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRepository
import org.springframework.transaction.annotation.Transactional

interface TenantInvitationService : CachedBaseService<TenantInvitationRepository, TenantInvitationEntity> {
    suspend fun getInvitationByCode(code: String): TenantInvitationEntity

    @Transactional(rollbackFor = [Exception::class])
    suspend fun acceptInvitation(userId: Long, invitationCode: String, realName: String, phoneNumber: String)
}