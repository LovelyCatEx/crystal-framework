package com.lovelycatv.crystalframework.message.service.impl

import com.lovelycatv.crystalframework.message.entity.MsgConversationEntity
import com.lovelycatv.crystalframework.message.repository.MsgConversationRepository
import com.lovelycatv.crystalframework.message.repository.MsgConversationPartyRepository
import com.lovelycatv.crystalframework.message.service.MsgConversationService
import com.lovelycatv.crystalframework.message.types.ConversationKind
import com.lovelycatv.crystalframework.sdk.message.Party
import com.lovelycatv.crystalframework.sdk.message.PartyResolverRegistry
import com.lovelycatv.crystalframework.sdk.message.Scope
import com.lovelycatv.crystalframework.sdk.message.types.PartyType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MsgConversationServiceImpl(
    private val msgConversationRepository: MsgConversationRepository,
    private val msgConversationPartyRepository: MsgConversationPartyRepository,
    private val partyResolverRegistry: PartyResolverRegistry,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : MsgConversationService {
    override fun getRepository(): MsgConversationRepository = msgConversationRepository

    override val cacheStore: ReactiveExpiringKVStore<String, MsgConversationEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<MsgConversationEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<MsgConversationEntity> = MsgConversationEntity::class

    override suspend fun getByDedupeKey(dedupeKey: String): MsgConversationEntity? =
        msgConversationRepository.findByDedupeKey(dedupeKey).awaitFirstOrNull()

    override suspend fun createConversation(
        scope: Scope,
        kind: ConversationKind,
        dedupeKey: String,
    ): MsgConversationEntity =
        msgConversationRepository.save(
            MsgConversationEntity(
                id = snowIdGenerator.nextId(),
                scopeType = scope.type.typeId,
                scopeId = scope.id,
                conversationKind = kind.typeId,
                dedupeKey = dedupeKey,
            ).apply { newEntity() }
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create conversation")

    override suspend fun updateLastMessage(
        conversationId: Long,
        messageId: Long,
        messageTime: Long,
    ): MsgConversationEntity? =
        withUpdateEntityContext(conversationId) {
            withUpdateById(conversationId) {
                lastMessageId = messageId
                lastMessageTime = messageTime
            }
        }

    override suspend fun isTenantServiceDeskConversation(conversationId: Long): Boolean =
        listParties(conversationId).any { it.type == PartyType.TENANT }

    override suspend fun isCurrentTenantServiceDeskRecipient(conversationId: Long, userId: Long): Boolean =
        listParties(conversationId).any { party ->
            userId in partyResolverRegistry.resolve(party.type).resolveRecipients(party)
        }

    private suspend fun listParties(conversationId: Long): List<Party> =
        msgConversationPartyRepository.findAllByConversationId(conversationId)
            .collectList()
            .awaitFirstOrNull()
            ?.map { party -> Party(party.getRealPartyType(), party.partyId) }
            ?: emptyList()
}
