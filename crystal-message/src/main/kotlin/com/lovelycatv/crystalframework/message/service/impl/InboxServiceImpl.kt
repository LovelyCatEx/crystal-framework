package com.lovelycatv.crystalframework.message.service.impl

import com.lovelycatv.crystalframework.message.controller.vo.ConversationInboxVO
import com.lovelycatv.crystalframework.message.entity.MsgConversationPartyEntity
import com.lovelycatv.crystalframework.message.repository.MsgConversationPartyRepository
import com.lovelycatv.crystalframework.message.service.BroadcastService
import com.lovelycatv.crystalframework.message.service.InboxService
import com.lovelycatv.crystalframework.message.service.MsgConversationMemberService
import com.lovelycatv.crystalframework.message.service.MsgConversationService
import com.lovelycatv.crystalframework.sdk.message.Party
import com.lovelycatv.crystalframework.sdk.message.PartyResolverRegistry
import com.lovelycatv.crystalframework.sdk.message.Scope
import com.lovelycatv.crystalframework.sdk.message.ScopeResolverRegistry
import com.lovelycatv.crystalframework.sdk.message.types.PartyType
import com.lovelycatv.crystalframework.sdk.message.types.ScopeType
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class InboxServiceImpl(
    private val msgConversationMemberService: MsgConversationMemberService,
    private val msgConversationService: MsgConversationService,
    private val msgConversationPartyRepository: MsgConversationPartyRepository,
    private val partyResolverRegistry: PartyResolverRegistry,
    private val scopeResolverRegistry: ScopeResolverRegistry,
    private val broadcastService: BroadcastService,
) : InboxService {

    override suspend fun listConversations(userId: Long, currentTenantId: Long?): List<ConversationInboxVO> =
        msgConversationMemberService.listByUser(userId)
            .flatMap { member ->
                val conversation = msgConversationService.getByIdOrNull(member.conversationId)
                    ?: return@flatMap emptyList()
                val parties = msgConversationPartyRepository.findAllByConversationId(member.conversationId)
                    .collectList().awaitFirstOrNull().orEmpty()
                viewingParties(userId, parties).map { viewing ->
                    val counterpart = parties.firstOrNull { it !== viewing }
                    ConversationInboxVO(
                        conversationId = member.conversationId,
                        unreadCount = member.unreadCount,
                        scopeType = conversation.scopeType,
                        scopeId = conversation.scopeId,
                        viewingPartyType = viewing.partyType,
                        viewingPartyId = viewing.partyId,
                        viewingPartyName = resolveName(viewing),
                        counterpartType = counterpart?.partyType ?: PartyType.SYSTEM.typeId,
                        counterpartId = counterpart?.partyId,
                        counterpartName = counterpart?.let { resolveName(it) },
                        counterpartInCurrentOrg = isCounterpartInCurrentOrg(currentTenantId, counterpart),
                        lastMessageTime = conversation.lastMessageTime,
                    )
                }
            }
            .sortedByDescending { it.lastMessageTime ?: Long.MIN_VALUE }

    /**
     * Whether [counterpart] is a member of the tenant the caller is currently acting as. Only a plain
     * USER counterpart can be an org member; a TENANT/SYSTEM face is never "internal". Returns false
     * when the caller supplied no acting tenant (plain system-user session — everything is external).
     */
    private suspend fun isCounterpartInCurrentOrg(
        currentTenantId: Long?,
        counterpart: MsgConversationPartyEntity?,
    ): Boolean {
        if (currentTenantId == null || counterpart == null) return false
        if (counterpart.getRealPartyType() != PartyType.USER) return false
        val counterpartUserId = counterpart.partyId ?: return false
        return scopeResolverRegistry.resolve(ScopeType.TENANT)
            .isMember(Scope(ScopeType.TENANT, currentTenantId), counterpartUserId)
    }

    /**
     * The identities the user acts *as* in this conversation: every seated party whose recipient set
     * contains the user. A plain peer or customer yields one (their USER party); a receptionist yields
     * the TENANT desk; a user who staffs the very desk they contacted yields both — so the same
     * conversation surfaces once in their personal inbox and once in that desk's inbox.
     */
    private suspend fun viewingParties(
        userId: Long,
        parties: List<MsgConversationPartyEntity>,
    ): List<MsgConversationPartyEntity> = parties.filter { party ->
        userId in partyResolverRegistry.resolve(party.getRealPartyType()).resolveRecipients(party.toParty())
    }

    private suspend fun resolveName(party: MsgConversationPartyEntity): String? =
        partyResolverRegistry.resolve(party.getRealPartyType()).resolveDisplayName(party.toParty())

    private fun MsgConversationPartyEntity.toParty(): Party = Party(getRealPartyType(), partyId)

    override suspend fun totalUnread(userId: Long, tenantIds: Collection<Long>): Long {
        val conversationUnread = msgConversationMemberService.listByUser(userId)
            .sumOf { it.unreadCount.toLong() }
        return conversationUnread + broadcastService.unreadCount(userId, tenantIds)
    }
}
