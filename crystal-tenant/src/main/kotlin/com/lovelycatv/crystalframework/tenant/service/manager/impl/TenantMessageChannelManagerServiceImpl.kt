package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
import com.lovelycatv.crystalframework.messagechannel.utils.ChannelConfigCodec
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto.ManagerCreateTenantMessageChannelDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto.ManagerUpdateTenantMessageChannelDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantMessageChannelEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMessageChannelRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMessageChannelManagerService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantMessageChannelManagerServiceImpl(
    private val tenantMessageChannelRepository: TenantMessageChannelRepository,
    private val channelConfigCodec: ChannelConfigCodec,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : TenantMessageChannelManagerService {

    override val cacheStore: ReactiveExpiringKVStore<String, TenantMessageChannelEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantMessageChannelEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantMessageChannelEntity> = TenantMessageChannelEntity::class

    override fun getRepository(): TenantMessageChannelRepository = tenantMessageChannelRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateTenantMessageChannelDTO): TenantMessageChannelEntity {
        val channelType = ChannelType.fromTypeId(dto.channelType)
            ?: throw BusinessException("Unknown channelType=${dto.channelType}")

        val config = parseConfigOrThrow(dto.config, channelType)

        val duplicate = tenantMessageChannelRepository
            .findByTenantIdAndChannelTypeAndName(dto.tenantId, channelType.typeId, dto.name)
            .awaitFirstOrNull()
        if (duplicate != null) {
            throw BusinessException("Channel name '${dto.name}' already exists for channelType=$channelType")
        }

        val entity = TenantMessageChannelEntity(
            id = snowIdGenerator.nextId(),
            tenantId = dto.tenantId,
            channelType = channelType.typeId,
            name = dto.name,
            enabled = dto.enabled,
            config = channelConfigCodec.encode(config),
        ).apply { newEntity() }

        return tenantMessageChannelRepository.save(entity).awaitFirstOrNull()
            ?: throw BusinessException("Could not create tenant message channel")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateTenantMessageChannelDTO,
        original: TenantMessageChannelEntity,
    ): TenantMessageChannelEntity {
        dto.name?.let { newName ->
            if (newName != original.name) {
                val duplicate = tenantMessageChannelRepository
                    .findByTenantIdAndChannelTypeAndName(original.tenantId, original.channelType, newName)
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

    override suspend fun findAllByTenantId(tenantId: Long): List<TenantMessageChannelEntity> {
        return getRepository().findAllByTenantId(tenantId).awaitListWithTimeout()
    }

    override suspend fun resolveConfig(channelId: Long): ChannelConfig {
        val entity = getByIdOrThrow(channelId)
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
