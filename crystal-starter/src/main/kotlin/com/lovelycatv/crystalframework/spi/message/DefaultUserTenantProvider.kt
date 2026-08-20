package com.lovelycatv.crystalframework.spi.message

import com.lovelycatv.crystalframework.sdk.message.config.UserTenantProvider
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRelationService
import org.springframework.stereotype.Component

/**
 * Composition-root implementation of [UserTenantProvider]: resolves a user's tenant
 * membership through the tenant module, keeping that dependency out of the messaging
 * core. Mirrors how the message party / scope / audience resolvers are wired here.
 */
@Component
class DefaultUserTenantProvider(
    private val tenantMemberRelationService: TenantMemberRelationService,
) : UserTenantProvider {
    override suspend fun tenantIdsOf(userId: Long): Collection<Long> =
        tenantMemberRelationService.getUserTenantMembers(userId).map { it.tenantId }.toSet()
}
