package com.lovelycatv.crystalframework.spi.message.scope

import com.lovelycatv.crystalframework.sdk.message.Scope
import com.lovelycatv.crystalframework.sdk.message.config.MessageScopeResolver
import com.lovelycatv.crystalframework.sdk.message.types.ScopeType
import org.springframework.stereotype.Component

/**
 * The global (platform) messaging boundary. Every authenticated user belongs to
 * it, so system-scope conversations are never isolated by membership. Being
 * global, this scope is never revoked.
 */
@Component
class SystemMessageScopeResolver : MessageScopeResolver {
    override val scopeType: ScopeType = ScopeType.SYSTEM

    override suspend fun isMember(scope: Scope, userId: Long): Boolean = true

    override suspend fun onScopeRevoked(scope: Scope) = Unit
}
