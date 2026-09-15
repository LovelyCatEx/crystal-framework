/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.economy

/**
 * SPI for resolving the tenant member id of a user within a tenant.
 *
 * `crystal-economy` depends on `crystal-sdk` but not on `crystal-tenant` (which owns
 * `TenantMemberService`), so it cannot resolve the member id directly. This interface inverts
 * that dependency: `crystal-sdk` declares the contract and `crystal-tenant` provides the
 * `@Component` implementation.
 *
 * When no implementation is present (tenant module disabled), or the user is not a member of the
 * tenant, the consumer ([com.lovelycatv.crystalframework.economy.service.EconomyWalletService])
 * skips the tenant-level wallet and falls back to the user's system wallet.
 */
fun interface TenantMemberIdResolver {
    /**
     * @return the tenant member id for [userId] within [tenantId], or null when no membership exists.
     */
    suspend fun resolveMemberId(tenantId: Long, userId: Long): Long?
}
