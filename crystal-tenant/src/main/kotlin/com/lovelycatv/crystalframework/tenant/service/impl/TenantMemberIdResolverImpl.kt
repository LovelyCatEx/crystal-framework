/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.sdk.economy.TenantMemberIdResolver
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import org.springframework.stereotype.Component

/**
 * crystal-tenant side implementation of the economy module's [TenantMemberIdResolver] SPI.
 *
 * crystal-economy cannot depend on crystal-tenant to reach [TenantMemberService], so the member id
 * lookup is inverted through this SPI: crystal-sdk declares the contract, crystal-tenant fulfils it.
 */
@Component
class TenantMemberIdResolverImpl(
    private val tenantMemberService: TenantMemberService,
) : TenantMemberIdResolver {
    override suspend fun resolveMemberId(tenantId: Long, userId: Long): Long? {
        return tenantMemberService.getByTenantIdAndUserId(tenantId, userId)?.id
    }
}
