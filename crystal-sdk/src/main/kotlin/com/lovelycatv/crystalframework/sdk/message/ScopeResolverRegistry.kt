package com.lovelycatv.crystalframework.sdk.message

import com.lovelycatv.crystalframework.sdk.message.config.MessageScopeResolver
import com.lovelycatv.crystalframework.sdk.message.types.ScopeType

/**
 * Holds one [MessageScopeResolver] per [ScopeType]. Assembled once at startup in
 * crystal-starter from all resolver beans; fails fast on a duplicate type so
 * conflicts surface at boot rather than at runtime.
 */
class ScopeResolverRegistry(resolvers: Iterable<MessageScopeResolver>) {
    private val byType = linkedMapOf<ScopeType, MessageScopeResolver>()

    init {
        resolvers.forEach { resolver ->
            if (byType.putIfAbsent(resolver.scopeType, resolver) != null) {
                throw IllegalStateException(
                    "ScopeResolverRegistry: duplicate resolver for scope type '${resolver.scopeType}'"
                )
            }
        }
    }

    fun resolve(type: ScopeType): MessageScopeResolver =
        byType[type] ?: throw IllegalStateException(
            "ScopeResolverRegistry: no resolver registered for scope type '$type'"
        )

    fun resolvers(): List<MessageScopeResolver> = byType.values.toList()
}
