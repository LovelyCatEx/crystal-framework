package com.lovelycatv.crystalframework.sdk.message

import com.lovelycatv.crystalframework.sdk.message.config.MessageAudienceResolver
import com.lovelycatv.crystalframework.sdk.message.types.AudienceType

/**
 * Holds one [MessageAudienceResolver] per [AudienceType]. Assembled once at startup
 * in crystal-starter from all resolver beans; fails fast on a duplicate type so
 * conflicts surface at boot rather than at runtime.
 */
class AudienceResolverRegistry(resolvers: Iterable<MessageAudienceResolver>) {
    private val byType = linkedMapOf<AudienceType, MessageAudienceResolver>()

    init {
        resolvers.forEach { resolver ->
            if (byType.putIfAbsent(resolver.audienceType, resolver) != null) {
                throw IllegalStateException(
                    "AudienceResolverRegistry: duplicate resolver for audience type '${resolver.audienceType}'"
                )
            }
        }
    }

    fun resolve(type: AudienceType): MessageAudienceResolver =
        byType[type] ?: throw IllegalStateException(
            "AudienceResolverRegistry: no resolver registered for audience type '$type'"
        )

    fun resolvers(): List<MessageAudienceResolver> = byType.values.toList()
}
