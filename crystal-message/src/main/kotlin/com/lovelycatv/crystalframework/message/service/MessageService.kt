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
     */
    suspend fun send(
        scope: Scope,
        sender: Party,
        target: Party,
        content: String,
        contentType: ContentType,
        actingUserId: Long,
    ): MsgMessageEntity

    /** Page a conversation's messages (newest first). */
    suspend fun getConversationMessages(
        conversationId: Long,
        page: Int,
        pageSize: Int,
    ): PaginatedResponseData<MsgMessageEntity>

    /** Mark a conversation read for [userId], advancing the read cursor to its last message. */
    suspend fun markConversationRead(conversationId: Long, userId: Long)
}
