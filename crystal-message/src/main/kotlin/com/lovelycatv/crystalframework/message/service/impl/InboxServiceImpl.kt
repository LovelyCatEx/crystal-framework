package com.lovelycatv.crystalframework.message.service.impl

import com.lovelycatv.crystalframework.message.entity.MsgConversationMemberEntity
import com.lovelycatv.crystalframework.message.service.BroadcastService
import com.lovelycatv.crystalframework.message.service.InboxService
import com.lovelycatv.crystalframework.message.service.MsgConversationMemberService
import com.lovelycatv.crystalframework.message.service.MsgConversationService
import org.springframework.stereotype.Service

@Service
class InboxServiceImpl(
    private val msgConversationMemberService: MsgConversationMemberService,
    private val msgConversationService: MsgConversationService,
    private val broadcastService: BroadcastService,
) : InboxService {

    override suspend fun listConversations(userId: Long): List<MsgConversationMemberEntity> {
        val members = msgConversationMemberService.listByUser(userId)
        val lastMessageTimeByConversation = members
            .associate { it.conversationId to (msgConversationService.getByIdOrNull(it.conversationId)?.lastMessageTime) }
        return members.sortedByDescending { lastMessageTimeByConversation[it.conversationId] ?: Long.MIN_VALUE }
    }

    override suspend fun totalUnread(userId: Long, tenantIds: Collection<Long>): Long {
        val conversationUnread = msgConversationMemberService.listByUser(userId)
            .sumOf { it.unreadCount.toLong() }
        return conversationUnread + broadcastService.unreadCount(userId, tenantIds)
    }
}
