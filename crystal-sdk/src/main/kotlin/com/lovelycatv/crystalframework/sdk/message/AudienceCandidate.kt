package com.lovelycatv.crystalframework.sdk.message

/**
 * The user being tested for broadcast membership, plus the context a resolver
 * needs to decide (the tenants the user belongs to). The caller supplies the
 * tenant set so resolvers stay lookup-free and the messaging core stays
 * tenant-agnostic.
 */
data class AudienceCandidate(
    val userId: Long,
    val tenantIds: Collection<Long>,
)
