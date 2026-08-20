package com.lovelycatv.crystalframework.spi.message.party

import com.lovelycatv.crystalframework.sdk.message.Party
import com.lovelycatv.crystalframework.sdk.message.config.MessagePartyResolver
import com.lovelycatv.crystalframework.sdk.message.types.PartyType
import com.lovelycatv.crystalframework.sdk.message.types.ReadGranularity
import org.springframework.stereotype.Component

/**
 * The platform itself. The system speaks through read-diffusion broadcasts,
 * not through point-to-point conversations, so no human user may act as SYSTEM in
 * the conversation send path and there are no per-conversation recipients to
 * expand here.
 */
@Component
class SystemMessagePartyResolver : MessagePartyResolver {
    override val partyType: PartyType = PartyType.SYSTEM
    override val readGranularity: ReadGranularity = ReadGranularity.PER_USER

    override suspend fun resolveRecipients(party: Party): Collection<Long> = emptyList()

    override suspend fun isAvailable(party: Party): Boolean = false

    override suspend fun canActAs(party: Party, userId: Long): Boolean = false

    override suspend fun resolveDisplayName(party: Party): String? = null
}
