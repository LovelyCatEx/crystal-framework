package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerCreateTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerUpdateTenantDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantManagerServiceImpl(
    private val tenantRepository: TenantRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantManagerService {
    override val cacheStore: ExpiringKVStore<String, TenantEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantEntity> = TenantEntity::class

    override fun getRepository(): TenantRepository {
        return tenantRepository
    }

    override suspend fun create(dto: ManagerCreateTenantDTO): TenantEntity {
        val entity = TenantEntity(
            id = snowIdGenerator.nextId(),
            ownerUserId = dto.ownerUserId,
            name = dto.name,
            description = dto.description,
            tireTypeId = dto.tireTypeId,
            subscribedTime = dto.subscribedTime,
            expiresTime = dto.expiresTime,
            contactName = dto.contactName,
            contactEmail = dto.contactEmail,
            contactPhone = dto.contactPhone,
            address = dto.address,
            settings = dto.settings
        ).apply { newEntity() }
        return tenantRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create tenant")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateTenantDTO, original: TenantEntity): TenantEntity {
        return original.apply {
            dto.name?.let { name = it }
            dto.description?.let { description = it }
            dto.status?.let { status = it }
            dto.tireTypeId?.let { tireTypeId = it }
            dto.subscribedTime?.let { subscribedTime = it }
            dto.expiresTime?.let { expiresTime = it }
            dto.contactName?.let { contactName = it }
            dto.contactEmail?.let { contactEmail = it }
            dto.contactPhone?.let { contactPhone = it }
            dto.address?.let { address = it }
            dto.settings?.let { settings = it }
        }
    }
}
