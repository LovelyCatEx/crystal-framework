package com.lovelycatv.crystalframework.sdk.message

import com.lovelycatv.crystalframework.sdk.message.config.MessagePartyResolver
import com.lovelycatv.crystalframework.sdk.message.types.PartyType

/**
 * Holds one [MessagePartyResolver] per [PartyType]. Assembled once at startup in
 * crystal-starter from all resolver beans; fails fast on a duplicate type so
 * conflicts surface at boot rather than at runtime.
 */
class PartyResolverRegistry(resolvers: Iterable<MessagePartyResolver>) {
    private val byType = linkedMapOf<PartyType, MessagePartyResolver>()

    init {
        resolvers.forEach { resolver ->
            if (byType.putIfAbsent(resolver.partyType, resolver) != null) {
                throw IllegalStateException(
                    "PartyResolverRegistry: duplicate resolver for party type '${resolver.partyType}'"
                )
            }
        }
    }

    fun resolve(type: PartyType): MessagePartyResolver =
        byType[type] ?: throw IllegalStateException(
            "PartyResolverRegistry: no resolver registered for party type '$type'"
        )

    fun resolvers(): List<MessagePartyResolver> = byType.values.toList()
}
