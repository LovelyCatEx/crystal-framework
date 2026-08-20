package com.lovelycatv.crystalframework.message.service

import com.lovelycatv.crystalframework.message.entity.MsgMessageEntity
import com.lovelycatv.crystalframework.message.types.ContentType
import com.lovelycatv.crystalframework.sdk.message.Party
import com.lovelycatv.crystalframework.sdk.message.Scope
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData

/**
 * The messaging core. Completely scope- and party-agnostic: it authorizes and
 * routes purely through the [com.lovelycatv.crystalframework.sdk.message]
 * resolver registries, so new scope / party types never touch this class.
 */
interface MessageService {
    /**
     * Send a direct (point-to-point) message from [sender] to [target] within [scope],
     * physically performed by real user [actingUserId]. Finds or creates the isolated
     * conversation, persists one message copy, and updates recipients' unread state.
     *
     * [enforceScopeMembership] gates the "acting user belongs to the scope" precheck.
     * It stays true for intra-scope messaging (a member writing inside their own
     * tenant). It is set false for the "outside user contacts a tenant's
     * customer-service desk" case, where the sender is legitimately not a member of
     * the target tenant scope; the sender's authority is still fully enforced by the
     * sender party's [com.lovelycatv.crystalframework.sdk.message.config.MessagePartyResolver.canActAs].
     *
     * [enforceTargetScopeMembership] additionally requires a USER target to belong
     * to [scope], which is used for tenant-internal member conversations.
     */
    suspend fun send(
        scope: Scope,
        sender: Party,
        target: Party,
        content: String,
        contentType: ContentType,
        actingUserId: Long,
        enforceScopeMembership: Boolean = true,
        enforceTargetScopeMembership: Boolean = false,
    ): MsgMessageEntity

    /** Reject access when the feature owning this persisted conversation is disabled. */
    suspend fun assertConversationFeatureEnabled(conversationId: Long)

    /** Page a conversation's messages (newest first). */
    suspend fun getConversationMessages(
        conversationId: Long,
        page: Int,
        pageSize: Int,
    ): PaginatedResponseData<MsgMessageEntity>

    /** Mark a conversation read for [userId], advancing the read cursor to its last message. */
    suspend fun markConversationRead(conversationId: Long, userId: Long)
}
