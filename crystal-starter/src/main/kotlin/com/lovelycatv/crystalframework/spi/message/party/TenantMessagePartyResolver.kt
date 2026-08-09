package com.lovelycatv.crystalframework.spi.message.party

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.sdk.message.Party
import com.lovelycatv.crystalframework.sdk.message.config.MessagePartyResolver
import com.lovelycatv.crystalframework.sdk.message.types.PartyType
import com.lovelycatv.crystalframework.sdk.message.types.ReadGranularity
import com.lovelycatv.crystalframework.tenant.service.TenantService
import org.springframework.stereotype.Component

/**
 * A tenant (store / customer-service desk). The tenant is a SHARED party: a
 * message toward it fans out only to the members holding the customer-service
 * reception permission ([TenantPermission.ACTION_MESSAGE_RECEPTION_HANDLE]), and
 * only such a member may act as (reply on behalf of) the tenant. Those members
 * share one read cursor — the shared CS inbox — which is exactly
 * [ReadGranularity.SHARED].
 */
@Component
class TenantMessagePartyResolver(
    private val tenantService: TenantService,
) : MessagePartyResolver {
    override val partyType: PartyType = PartyType.TENANT
    override val readGranularity: ReadGranularity = ReadGranularity.SHARED

    override suspend fun resolveRecipients(party: Party): Collection<Long> {
        val tenantId = party.id ?: return emptyList()
        return receptionistUserIds(tenantId)
    }

    override suspend fun canActAs(party: Party, userId: Long): Boolean {
        val tenantId = party.id ?: return false
        return userId in receptionistUserIds(tenantId)
    }

    override suspend fun resolveDisplayName(party: Party): String? =
        party.id?.let { tenantService.getByIdOrNull(it)?.name }

    private suspend fun receptionistUserIds(tenantId: Long): Set<Long> =
        tenantService
            .getMembersHasAnyPermission(tenantId, TenantPermission.ACTION_MESSAGE_RECEPTION_HANDLE.name)
            .map { it.memberUserId }
            .toSet()
}
