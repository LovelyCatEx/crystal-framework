package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.controller.manager.tiretype.dto.ManagerCreateTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tiretype.dto.ManagerUpdateTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantTireTypeEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireTypeRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireTypeManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantTireTypeManagerServiceImpl(
    private val tenantTireTypeRepository: TenantTireTypeRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantTireTypeManagerService {
    override val cacheStore: ExpiringKVStore<String, TenantTireTypeEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantTireTypeEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantTireTypeEntity> = TenantTireTypeEntity::class

    override fun getRepository(): TenantTireTypeRepository {
        return tenantTireTypeRepository
    }

    override suspend fun create(dto: ManagerCreateTenantTireTypeDTO): TenantTireTypeEntity {
        val entity = TenantTireTypeEntity(
            id = snowIdGenerator.nextId(),
            name = dto.name,
            description = dto.description
        ).apply { newEntity() }
        return tenantTireTypeRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create tenant tire type")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateTenantTireTypeDTO, original: TenantTireTypeEntity): TenantTireTypeEntity {
        return original.apply {
            dto.name?.let { name = it }
            dto.description?.let { description = it }
        }
    }
}
