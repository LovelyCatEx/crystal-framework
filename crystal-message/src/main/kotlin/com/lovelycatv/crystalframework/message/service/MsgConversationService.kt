package com.lovelycatv.crystalframework.message.service

import com.lovelycatv.crystalframework.message.entity.MsgConversationEntity
import com.lovelycatv.crystalframework.message.repository.MsgConversationRepository
import com.lovelycatv.crystalframework.message.types.ConversationKind
import com.lovelycatv.crystalframework.sdk.message.Scope
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface MsgConversationService :
    CachedBaseService<MsgConversationRepository, MsgConversationEntity> {
    /** Look up an existing conversation by its dedupe key (the scope + kind + parties identity). */
    suspend fun getByDedupeKey(dedupeKey: String): MsgConversationEntity?

    /** Create a fresh conversation for the given scope / kind / dedupe key. */
    suspend fun createConversation(
        scope: Scope,
        kind: ConversationKind,
        dedupeKey: String,
    ): MsgConversationEntity

    /** Advance the conversation's last-message pointer (cache-safe update). */
    suspend fun updateLastMessage(
        conversationId: Long,
        messageId: Long,
        messageTime: Long,
    ): MsgConversationEntity?

    /** Whether this conversation includes a tenant service-desk party. */
    suspend fun isTenantServiceDeskConversation(conversationId: Long): Boolean

    /** Whether [userId] is currently an eligible receptionist for this service-desk conversation. */
    suspend fun isCurrentTenantServiceDeskRecipient(conversationId: Long, userId: Long): Boolean
}