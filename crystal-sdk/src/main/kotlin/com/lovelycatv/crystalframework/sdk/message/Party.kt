package com.lovelycatv.crystalframework.sdk.message

import com.lovelycatv.crystalframework.sdk.message.types.PartyType

/**
 * A participant in a conversation — the outward-facing "who".
 *
 * @property type  the kind of participant.
 * @property id    the entity id for that type (userId / tenantId); null for [PartyType.SYSTEM].
 */
data class Party(
    val type: PartyType,
    val id: Long?,
)
