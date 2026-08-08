package com.lovelycatv.crystalframework.sdk.message.types

/**
 * How a broadcast's target audience is defined (read-fanout side).
 *
 * Orthogonal to [ScopeType] / [PartyType]: scope answers "inside which isolation
 * boundary", party answers "who is speaking", audience answers "who receives".
 * New audience types are added by registering a new
 * [com.lovelycatv.crystalframework.sdk.message.config.MessageAudienceResolver] —
 * the messaging core never changes.
 */
enum class AudienceType(val typeId: Int) {
    /** Every user on the platform. */
    ALL_USERS(0),

    /** All members of the tenant identified by the audience ref. */
    TENANT_MEMBERS(1),

    /** A pre-defined segment referenced by the audience ref. Reserved. */
    SEGMENT(2);

    companion object {
        fun getByTypeId(typeId: Int): AudienceType? = entries.find { it.typeId == typeId }
    }
}
