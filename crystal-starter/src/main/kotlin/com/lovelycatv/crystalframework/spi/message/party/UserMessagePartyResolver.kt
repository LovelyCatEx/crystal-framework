package com.lovelycatv.crystalframework.spi.message.party

import com.lovelycatv.crystalframework.sdk.message.Party
import com.lovelycatv.crystalframework.sdk.message.config.MessagePartyResolver
import com.lovelycatv.crystalframework.sdk.message.types.PartyType
import com.lovelycatv.crystalframework.sdk.message.types.ReadGranularity
import com.lovelycatv.crystalframework.user.service.UserService
import org.springframework.stereotype.Component

/**
 * A single real user speaking as themselves. Recipients are just that user, and
 * only that user may act as the party. Each user keeps an independent read cursor.
 */
@Component
class UserMessagePartyResolver(
    private val userService: UserService,
) : MessagePartyResolver {
    override val partyType: PartyType = PartyType.USER
    override val readGranularity: ReadGranularity = ReadGranularity.PER_USER

    override suspend fun resolveRecipients(party: Party): Collection<Long> =
        party.id?.let { listOf(it) } ?: emptyList()

    override suspend fun canActAs(party: Party, userId: Long): Boolean = party.id == userId

    override suspend fun resolveDisplayName(party: Party): String? =
        party.id?.let { userService.getByIdOrNull(it)?.nickname }
}
