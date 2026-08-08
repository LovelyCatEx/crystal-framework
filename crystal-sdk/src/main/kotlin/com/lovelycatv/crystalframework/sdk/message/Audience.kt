package com.lovelycatv.crystalframework.sdk.message

import com.lovelycatv.crystalframework.sdk.message.types.AudienceType

/**
 * The target-audience definition carried by a broadcast (read-fanout side): a
 * [type] plus an optional [ref] that the resolver for that type interprets (e.g. a
 * tenantId for [AudienceType.TENANT_MEMBERS]).
 */
data class Audience(
    val type: AudienceType,
    val ref: Long? = null,
)
