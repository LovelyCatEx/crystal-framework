package com.lovelycatv.crystalframework.message.service.impl

import com.lovelycatv.crystalframework.message.entity.MsgConversationMemberEntity
import com.lovelycatv.crystalframework.message.repository.MsgConversationMemberRepository
import com.lovelycatv.crystalframework.message.service.MsgConversationMemberService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MsgConversationMemberServiceImpl(
    private val msgConversationMemberRepository: MsgConversationMemberRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : MsgConversationMemberService {
    override fun getRepository(): MsgConversationMemberRepository = msgConversationMemberRepository

    override val cacheStore: ReactiveExpiringKVStore<String, MsgConversationMemberEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<MsgConversationMemberEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<MsgConversationMemberEntity> = MsgConversationMemberEntity::class

    override suspend fun getOrCreate(conversationId: Long, userId: Long): MsgConversationMemberEntity =
        msgConversationMemberRepository.findByConversationIdAndUserId(conversationId, userId).awaitFirstOrNull()
            ?: msgConversationMemberRepository.save(
                MsgConversationMemberEntity(
                    id = snowIdGenerator.nextId(),
                    conversationId = conversationId,
                    userId = userId,
                    unreadCount = 0,
                ).apply { newEntity() }
            ).awaitFirstOrNull() ?: throw BusinessException("Could not create conversation member")

    override suspend fun incrementUnread(conversationId: Long, userId: Long): MsgConversationMemberEntity? {
        val member = msgConversationMemberRepository
            .findByConversationIdAndUserId(conversationId, userId).awaitFirstOrNull() ?: return null
        withInvalidateEntityCacheContext(member.id) {
            msgConversationMemberRepository.incrementUnread(
                conversationId = conversationId,
                userId = userId,
                modifiedTime = System.currentTimeMillis(),
            ).awaitFirstOrNull()
        }
        return msgConversationMemberRepository.findByConversationIdAndUserId(conversationId, userId).awaitFirstOrNull()
    }

    override suspend fun markRead(
        conversationId: Long,
        userId: Long,
        lastReadMessageId: Long?,
    ): MsgConversationMemberEntity? {
        val member = msgConversationMemberRepository
            .findByConversationIdAndUserId(conversationId, userId).awaitFirstOrNull() ?: return null
        return withUpdateEntityContext(member.id) {
            withUpdateById(member.id) {
                unreadCount = 0
                this.lastReadMessageId = lastReadMessageId
            }
        }
    }

    override suspend fun listByUser(userId: Long): List<MsgConversationMemberEntity> =
        msgConversationMemberRepository.findAllByUserId(userId).collectList().awaitFirstOrNull() ?: emptyList()
}
