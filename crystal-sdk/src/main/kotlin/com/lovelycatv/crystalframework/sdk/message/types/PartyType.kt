package com.lovelycatv.crystalframework.sdk.message.types

/**
 * The outward-facing identity a message is sent as (the "face" the recipient sees).
 *
 * Orthogonal to [ScopeType]: a party answers "who is speaking", a scope answers
 * "inside which isolation boundary". New party types are added by registering a
 * new [com.lovelycatv.crystalframework.sdk.message.config.MessagePartyResolver].
 */
enum class PartyType(val typeId: Int) {
    /** A single real user, speaking as themselves. */
    USER(0),

    /** The platform itself (system announcements). No [partyId]. */
    SYSTEM(1),

    /** A tenant (store / customer-service desk). Members act on its behalf. */
    TENANT(2);

    companion object {
        fun getByTypeId(typeId: Int): PartyType? = entries.find { it.typeId == typeId }
    }
}
