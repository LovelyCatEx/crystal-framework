/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.resource.interfaces

/**
 * SPI for resolving whether a user is an active member of a tenant.
 *
 * `crystal-tenant` depends on `crystal-resource` (it drives the upload manager), so
 * `crystal-resource` cannot depend back on `crystal-tenant` to reach `TenantMemberService`.
 * This interface inverts that dependency: `crystal-resource` declares the contract and
 * `crystal-tenant` provides the `@Component` implementation.
 *
 * When no implementation is present (tenant module disabled), the consumer
 * ([com.lovelycatv.crystalframework.resource.service.ResourceAccessService]) treats
 * [ResourceVisibility.SCOPE_MEMBER] as fail-closed: access is denied.
 */
fun interface ResourceTenantMembershipChecker {
    /**
     * @return true when [userId] is an active member of the tenant identified by [tenantId].
     */
    suspend fun isActiveMember(tenantId: Long, userId: Long): Boolean
}
