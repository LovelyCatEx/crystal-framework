package com.lovelycatv.crystalframework.message.service.impl

import com.lovelycatv.crystalframework.message.entity.MsgConversationPartyEntity
import com.lovelycatv.crystalframework.message.entity.MsgMessageEntity
import com.lovelycatv.crystalframework.message.repository.MsgConversationPartyRepository
import com.lovelycatv.crystalframework.message.repository.MsgMessageRepository
import com.lovelycatv.crystalframework.message.service.MessageService
import com.lovelycatv.crystalframework.message.service.MsgConversationMemberService
import com.lovelycatv.crystalframework.message.service.MsgConversationService
import com.lovelycatv.crystalframework.message.types.ContentType
import com.lovelycatv.crystalframework.message.types.ConversationKind
import com.lovelycatv.crystalframework.message.utils.DedupeKeyBuilder
import com.lovelycatv.crystalframework.sdk.message.Party
import com.lovelycatv.crystalframework.sdk.message.PartyResolverRegistry
import com.lovelycatv.crystalframework.sdk.message.Scope
import com.lovelycatv.crystalframework.sdk.message.ScopeResolverRegistry
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.request.PageQuery
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MessageServiceImpl(
    private val partyResolverRegistry: PartyResolverRegistry,
    private val scopeResolverRegistry: ScopeResolverRegistry,
    private val msgConversationService: MsgConversationService,
    private val msgConversationMemberService: MsgConversationMemberService,
    private val msgConversationPartyRepository: MsgConversationPartyRepository,
    private val msgMessageRepository: MsgMessageRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : MessageService {

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun send(
        scope: Scope,
        sender: Party,
        target: Party,
        content: String,
        contentType: ContentType,
        actingUserId: Long,
    ): MsgMessageEntity {
        if (!scopeResolverRegistry.resolve(scope.type).isMember(scope, actingUserId)) {
            throw ForbiddenException("User $actingUserId is not a member of scope ${scope.key()}")
        }
        if (!partyResolverRegistry.resolve(sender.type).canActAs(sender, actingUserId)) {
            throw ForbiddenException("User $actingUserId cannot act as party ${sender.type}:${sender.id}")
        }

        val parties = listOf(sender, target)
        val dedupeKey = DedupeKeyBuilder.build(scope, ConversationKind.DIRECT, parties)
        val conversation = msgConversationService.getByDedupeKey(dedupeKey)
            ?: msgConversationService.createConversation(scope, ConversationKind.DIRECT, dedupeKey).also { created ->
                parties.forEach { party ->
                    msgConversationPartyRepository.save(
                        MsgConversationPartyEntity(
                            id = snowIdGenerator.nextId(),
                            conversationId = created.id,
                            partyType = party.type.typeId,
                            partyId = party.id,
                        ).apply { newEntity() }
                    ).awaitFirstOrNull()
                }
            }

        val message = msgMessageRepository.save(
            MsgMessageEntity(
                id = snowIdGenerator.nextId(),
                conversationId = conversation.id,
                senderPartyType = sender.type.typeId,
                senderPartyId = sender.id,
                actingUserId = actingUserId,
                contentType = contentType.typeId,
                content = content,
            ).apply { newEntity() }
        ).awaitFirstOrNull() ?: throw BusinessException("Could not persist message")

        msgConversationService.updateLastMessage(conversation.id, message.id, message.createdTime)

        val recipients = parties
            .flatMap { partyResolverRegistry.resolve(it.type).resolveRecipients(it) }
            .toSet()
        recipients.forEach { recipientId ->
            msgConversationMemberService.getOrCreate(conversation.id, recipientId)
            if (recipientId == actingUserId) {
                msgConversationMemberService.markRead(conversation.id, recipientId, message.id)
            } else {
                msgConversationMemberService.incrementUnread(conversation.id, recipientId)
            }
        }
        return message
    }

    override suspend fun getConversationMessages(
        conversationId: Long,
        page: Int,
        pageSize: Int,
    ): PaginatedResponseData<MsgMessageEntity> {
        val criteria = Criteria.where(COLUMN_CONVERSATION_ID).`is`(conversationId)
        val baseQuery = Query.query(criteria).sort(Sort.by(Sort.Direction.DESC, BaseEntity.CREATED_TIME))
        val total = r2dbcEntityTemplate.count(baseQuery, MsgMessageEntity::class.java).awaitFirstOrNull() ?: 0L
        val records = r2dbcEntityTemplate
            .select(baseQuery.limit(pageSize).offset(((page - 1) * pageSize).toLong()), MsgMessageEntity::class.java)
            .collectList()
            .awaitFirstOrNull() ?: emptyList()
        return PageQuery(page, pageSize).toPaginatedResponseData(total = total, records = records)
    }

    override suspend fun markConversationRead(conversationId: Long, userId: Long) {
        val conversation = msgConversationService.getByIdOrNull(conversationId)
            ?: throw BusinessException("Conversation $conversationId not found")
        msgConversationMemberService.markRead(conversationId, userId, conversation.lastMessageId)
    }

    companion object {
        private const val COLUMN_CONVERSATION_ID = "conversation_id"
    }
}
