package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRepository
import org.springframework.transaction.annotation.Transactional

interface TenantService : CachedBaseService<TenantRepository, TenantEntity> {
    suspend fun getUserTenants(userId: Long): List<TenantEntity>

    /**
     * Transfer ownership of tenant to another user.
     * Original owner's role will be cleared and reset to member.
     * Target user's role will also be cleared and reset to root.
     *
     * @param tenantId TenantId
     * @param targetUserId Target owner userId
     */
    @Transactional(rollbackFor = [Exception::class])
    suspend fun transferOwnership(tenantId: Long, targetUserId: Long)
}