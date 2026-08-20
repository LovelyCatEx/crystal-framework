package com.lovelycatv.crystalframework.sdk.message.config

import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData

/**
 * SPI that supplies a paginated list of tenants a user may initiate customer-service
 * conversations with. The messaging core injects this and stays tenant-agnostic; the
 * concrete implementation lives in the composition root (crystal-starter), backed by
 * the tenant module. Only basic tenant info (id / name) is exposed — no sensitive data.
 */
interface ContactableTenantProvider {
    /**
     * Page through all contactable tenants, optionally filtered by [keyword] (name substring).
     * Results are capped at [pageSize] ≤ 20 per call (enforced by implementation).
     */
    suspend fun pageContactableTenants(
        keyword: String?,
        page: Int,
        pageSize: Int,
    ): PaginatedResponseData<ContactableTenantView>
}

/**
 * Minimal read-only tenant view returned by [ContactableTenantProvider]. Deliberately
 * minimal — only id and name — to avoid leaking tenant-internal details through the
 * messaging surface.
 */
data class ContactableTenantView(
    val id: String,      // Long serialized as String (framework convention)
    val name: String,
)
