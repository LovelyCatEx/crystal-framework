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
