package com.lovelycatv.crystalframework.sdk.message.config

import com.lovelycatv.crystalframework.sdk.message.Scope
import com.lovelycatv.crystalframework.sdk.message.types.ScopeType

/**
 * Pluggable strategy for one [ScopeType]. Each concrete resolver is a Spring bean
 * collected into the scope resolver registry; adding a new isolation boundary
 * never touches the messaging core.
 *
 * Implementations live in the owning business module (e.g. crystal-message).
 */
interface MessageScopeResolver {
    /** The single scope type this resolver handles. */
    val scopeType: ScopeType

    /** Whether [userId] belongs to this isolation boundary (may send / receive within it). */
    suspend fun isMember(scope: Scope, userId: Long): Boolean

    /** Clean up conversations bound to a scope that is being revoked (tenant deleted, etc.). */
    suspend fun onScopeRevoked(scope: Scope)
}
