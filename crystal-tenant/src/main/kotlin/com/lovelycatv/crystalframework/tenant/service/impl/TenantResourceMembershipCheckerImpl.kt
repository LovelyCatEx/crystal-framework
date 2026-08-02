package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.resource.interfaces.ResourceTenantMembershipChecker
import com.lovelycatv.crystalframework.shared.types.tenant.TenantMemberStatus
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import org.springframework.stereotype.Component

/**
 * crystal-tenant side implementation of the resource module's [ResourceTenantMembershipChecker] SPI.
 *
 * crystal-resource cannot depend on crystal-tenant (the dependency runs the other way — tenant
 * uploads through the resource module), so the SCOPE_MEMBER visibility check is inverted through
 * this SPI: the resource module declares the contract, the tenant module fulfils it.
 */
@Component
class TenantResourceMembershipCheckerImpl(
    private val tenantMemberService: TenantMemberService,
) : ResourceTenantMembershipChecker {
    override suspend fun isActiveMember(tenantId: Long, userId: Long): Boolean {
        val member = tenantMemberService.getByTenantIdAndUserId(tenantId, userId)
            ?: return false
        return member.getRealStatus() == TenantMemberStatus.ACTIVE
    }
}
