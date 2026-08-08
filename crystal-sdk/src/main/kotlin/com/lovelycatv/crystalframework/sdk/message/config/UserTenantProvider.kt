package com.lovelycatv.crystalframework.sdk.message.config

/**
 * SPI that supplies the set of tenants a user belongs to, so the messaging core can
 * build an [com.lovelycatv.crystalframework.sdk.message.AudienceCandidate] without
 * depending on the tenant module. The messaging layer injects this and stays
 * tenant-agnostic; the concrete implementation lives in the composition root
 * (crystal-starter), backed by the tenant module.
 */
interface UserTenantProvider {
    /** The ids of every tenant [userId] is a member of (empty if none). */
    suspend fun tenantIdsOf(userId: Long): Collection<Long>
}
