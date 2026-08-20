package com.lovelycatv.crystalframework.sdk.message.config

import com.lovelycatv.crystalframework.sdk.message.Audience
import com.lovelycatv.crystalframework.sdk.message.AudienceCandidate
import com.lovelycatv.crystalframework.sdk.message.types.AudienceType

/**
 * Pluggable strategy for one [AudienceType] on the read-fanout side: given a
 * broadcast's [Audience] and a candidate reader, decide whether the reader is a
 * recipient. Each concrete resolver is a Spring bean collected into the audience
 * resolver registry; adding a new audience type never touches the messaging core.
 *
 * Implementations live in the owning business module (e.g. crystal-starter).
 */
interface MessageAudienceResolver {
    /** The single audience type this resolver handles. */
    val audienceType: AudienceType

    /** Whether [candidate] is a recipient of a broadcast targeting [audience]. */
    suspend fun matches(audience: Audience, candidate: AudienceCandidate): Boolean
}
