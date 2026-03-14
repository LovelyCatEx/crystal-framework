package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationRecordEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRecordRepository
import reactor.core.publisher.Flux

interface TenantInvitationRecordService : CachedBaseService<TenantInvitationRecordRepository, TenantInvitationRecordEntity> {
    suspend fun saveRecord(invitationId: Long, userId: Long, realName: String, phoneNumber: String): TenantInvitationRecordEntity

    suspend fun getAllByInvitationId(invitationId: Long): List<TenantInvitationRecordEntity>

    suspend fun getAllByUsedUserId(usedUserId: Long): List<TenantInvitationRecordEntity>
}
