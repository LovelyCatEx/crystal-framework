package com.lovelycatv.crystalframework.spi.message.party

import com.lovelycatv.crystalframework.sdk.message.Party
import com.lovelycatv.crystalframework.sdk.message.config.MessagePartyResolver
import com.lovelycatv.crystalframework.sdk.message.types.PartyType
import com.lovelycatv.crystalframework.sdk.message.types.ReadGranularity
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import org.springframework.stereotype.Component

/**
 * A tenant (store / customer-service desk). Members act on its behalf, so a
 * message sent as the tenant fans out to every tenant member, and any member may
 * act as the party. All members share one read cursor — the shared CS inbox —
 * which is exactly [ReadGranularity.SHARED].
 */
@Component
class TenantMessagePartyResolver(
    private val tenantMemberService: TenantMemberService,
) : MessagePartyResolver {
    override val partyType: PartyType = PartyType.TENANT
    override val readGranularity: ReadGranularity = ReadGranularity.SHARED

    override suspend fun resolveRecipients(party: Party): Collection<Long> {
        val tenantId = party.id ?: return emptyList()
        return tenantMemberService.listMemberUserIds(tenantId)
    }

    override suspend fun canActAs(party: Party, userId: Long): Boolean {
        val tenantId = party.id ?: return false
        return tenantMemberService.getByTenantIdAndUserId(tenantId, userId) != null
    }
}
