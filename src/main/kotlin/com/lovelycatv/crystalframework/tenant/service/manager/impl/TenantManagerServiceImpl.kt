package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerCreateTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerUpdateTenantDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRepository
import com.lovelycatv.crystalframework.tenant.service.TenantInitializeService
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantManagerServiceImpl(
    private val tenantRepository: TenantRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val tenantService: TenantService,
    private val tenantInitializeService: TenantInitializeService,
) : TenantManagerService {
    private val logger = logger()

    override val cacheStore: ExpiringKVStore<String, TenantEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantEntity> = TenantEntity::class

    override fun getRepository(): TenantRepository {
        return tenantRepository
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun create(dto: ManagerCreateTenantDTO): TenantEntity {
        val entity = TenantEntity(
            id = snowIdGenerator.nextId(),
            ownerUserId = dto.ownerUserId,
            name = dto.name,
            description = dto.description,
            status = dto.status,
            tireTypeId = dto.tireTypeId,
            subscribedTime = dto.subscribedTime,
            expiresTime = dto.expiresTime,
            contactName = dto.contactName,
            contactEmail = dto.contactEmail,
            contactPhone = dto.contactPhone,
            address = dto.address,
            settings = dto.settings
        ).apply { newEntity() }

        val saved = tenantRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create tenant")

        tenantInitializeService.initializeTenant(saved.id, dto.ownerUserId)

        logger.info("A new tenant was created, data: ${saved.toJSONString()}")

        return saved
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateTenantDTO, original: TenantEntity): TenantEntity {
        if (dto.ownerUserId != null) {
            tenantService.transferOwnership(dto.id, dto.ownerUserId)
        }

        return original.apply {
            dto.ownerUserId?.let { ownerUserId = it }
            dto.name?.let { name = it }
            description = dto.description
            dto.status?.let { status = it }
            dto.tireTypeId?.let { tireTypeId = it }
            dto.subscribedTime?.let { subscribedTime = it }
            dto.expiresTime?.let { expiresTime = it }
            dto.contactName?.let { contactName = it }
            dto.contactEmail?.let { contactEmail = it }
            dto.contactPhone?.let { contactPhone = it }
            dto.address?.let { address = it }
            settings = dto.settings
        }
    }
}
