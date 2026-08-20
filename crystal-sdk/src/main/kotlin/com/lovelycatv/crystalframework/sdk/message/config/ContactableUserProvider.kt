package com.lovelycatv.crystalframework.sdk.message.config

import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData

/**
 * SPI that resolves the users a given user may start a SYSTEM-scope peer conversation with.
 * The messaging core injects this and stays user-module-agnostic; the concrete implementation
 * lives in the composition root (crystal-starter), backed by the user module.
 *
 * Privacy policy: exact-match lookup only. A blank keyword yields an empty page — the directory is
 * never browsable — and a non-blank keyword resolves at most the single user whose username or
 * email matches it exactly. This keeps the endpoint from doubling as a full user directory.
 */
interface ContactableUserProvider {
    /**
     * Resolve contactable users for [currentUserId] by exact [keyword] (username or email).
     * Blank keyword ⇒ empty page. The current user is always excluded from results.
     * Results are capped at [pageSize] ≤ 20 per call (enforced by implementation).
     */
    suspend fun searchContactableUsers(
        currentUserId: Long,
        keyword: String?,
        page: Int,
        pageSize: Int,
    ): PaginatedResponseData<ContactableUserView>
}
