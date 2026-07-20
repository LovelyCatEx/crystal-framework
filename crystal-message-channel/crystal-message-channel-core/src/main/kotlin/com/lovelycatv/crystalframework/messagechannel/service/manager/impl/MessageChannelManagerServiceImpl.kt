package com.lovelycatv.crystalframework.messagechannel.service.manager.impl

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.controller.manager.dto.ManagerCreateMessageChannelDTO
import com.lovelycatv.crystalframework.messagechannel.controller.manager.dto.ManagerUpdateMessageChannelDTO
import com.lovelycatv.crystalframework.messagechannel.entity.MessageChannelEntity
import com.lovelycatv.crystalframework.messagechannel.repository.MessageChannelRepository
import com.lovelycatv.crystalframework.messagechannel.service.manager.MessageChannelManagerService
import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
import com.lovelycatv.crystalframework.messagechannel.utils.ChannelConfigCodec
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MessageChannelManagerServiceImpl(
    private val messageChannelRepository: MessageChannelRepository,
    private val channelConfigCodec: ChannelConfigCodec,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : MessageChannelManagerService {

    override val cacheStore: ReactiveExpiringKVStore<String, MessageChannelEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<MessageChannelEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<MessageChannelEntity> = MessageChannelEntity::class

    override fun getRepository(): MessageChannelRepository = messageChannelRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateMessageChannelDTO): MessageChannelEntity {
        val channelType = ChannelType.fromTypeId(dto.channelType)
            ?: throw BusinessException("Unknown channelType=${dto.channelType}")

        val config = parseConfigOrThrow(dto.config, channelType)

        val duplicate = messageChannelRepository
            .findByScopeAndScopeIdAndChannelTypeAndName(dto.scope, dto.scopeId, channelType.typeId, dto.name)
            .awaitFirstOrNull()
        if (duplicate != null) {
            throw BusinessException("Channel name '${dto.name}' already exists for channelType=$channelType")
        }

        val entity = MessageChannelEntity(
            id = snowIdGenerator.nextId(),
            scope = dto.scope,
            scopeId = dto.scopeId,
            channelType = channelType.typeId,
            name = dto.name,
            enabled = dto.enabled,
            config = channelConfigCodec.encode(config),
        ).apply { newEntity() }

        return messageChannelRepository.save(entity).awaitFirstOrNull()
            ?: throw BusinessException("Could not create message channel")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateMessageChannelDTO,
        original: MessageChannelEntity,
    ): MessageChannelEntity {
        dto.name?.let { newName ->
            if (newName != original.name) {
                val duplicate = messageChannelRepository
                    .findByScopeAndScopeIdAndChannelTypeAndName(
                        original.scope, original.scopeId, original.channelType, newName,
                    )
                    .awaitFirstOrNull()
                if (duplicate != null && duplicate.id != original.id) {
                    throw BusinessException(
                        "Channel name '$newName' already exists for channelType=${original.getRealChannelType()}"
                    )
                }
            }
        }

        return original.apply {
            dto.name?.let { name = it }
            dto.enabled?.let { enabled = it }
            dto.config?.let { rawJson ->
                val parsed = parseConfigOrThrow(rawJson, original.getRealChannelType())
                config = channelConfigCodec.encode(parsed)
            }
        }
    }

    override suspend fun findAllByScopeId(scopeId: Long): List<MessageChannelEntity> {
        return messageChannelRepository.findAllByScopeId(scopeId).awaitListWithTimeout()
    }

    override suspend fun resolveConfig(
        channelId: Long,
        expectedScope: ResourceScope,
        expectedScopeId: Long?,
    ): ChannelConfig {
        val entity = getByIdOrThrow(channelId)
        val entityScope = ResourceScope.getById(entity.scope)
            ?: throw BusinessException("Unknown channel scope ${entity.scope} for channel $channelId")
        if (entityScope != expectedScope || entity.scopeId != expectedScopeId) {
            throw ForbiddenException("Channel $channelId does not belong to the expected scope")
        }
        if (!entity.enabled) {
            throw BusinessException("Channel $channelId is disabled")
        }
        return channelConfigCodec.decode(entity.config, entity.getRealChannelType())
    }

    private fun parseConfigOrThrow(rawJson: String, channelType: ChannelType): ChannelConfig {
        val parsed = try {
            channelConfigCodec.fromPlainJson(rawJson, channelType)
        } catch (e: Exception) {
            throw BusinessException("Invalid config payload for channelType=$channelType: ${e.message}")
        }
        if (parsed.channelType != channelType) {
            throw BusinessException(
                "Config payload type ${parsed.channelType} mismatches channelType $channelType"
            )
        }
        return parsed
    }
}
