package com.lovelycatv.crystalframework.sdk.message.config

import com.lovelycatv.crystalframework.sdk.message.Party
import com.lovelycatv.crystalframework.sdk.message.types.PartyType
import com.lovelycatv.crystalframework.sdk.message.types.ReadGranularity

/**
 * Pluggable strategy for one [PartyType]. Each concrete resolver is a Spring bean
 * collected into the party resolver registry; adding a new party type never
 * touches the messaging core.
 *
 * Implementations live in the owning business module (e.g. crystal-message).
 */
interface MessagePartyResolver {
    /** The single party type this resolver handles. */
    val partyType: PartyType

    /** Read-cursor granularity for this party type (constant per type). */
    val readGranularity: ReadGranularity

    /** Expand the party into the concrete real users that should receive a message. */
    suspend fun resolveRecipients(party: Party): Collection<Long>

    /** Whether [userId] is allowed to send a message under this party's name (authorization). */
    suspend fun canActAs(party: Party, userId: Long): Boolean

    /**
     * The party's outward-facing display name (a conversation counterpart title in an
     * inbox list). Null when this party type has no meaningful name (e.g. SYSTEM) or the
     * referenced entity no longer exists — callers fall back to a generic label.
     */
    suspend fun resolveDisplayName(party: Party): String?
}
