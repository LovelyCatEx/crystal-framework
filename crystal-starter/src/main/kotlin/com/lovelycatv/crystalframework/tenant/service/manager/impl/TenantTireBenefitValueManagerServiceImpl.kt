package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerUpdateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantTireBenefitValueEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitValueRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitValueManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantTireBenefitValueManagerServiceImpl(
    private val benefitValueRepository: TenantTireBenefitValueRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : TenantTireBenefitValueManagerService {
    override val cacheStore: ExpiringKVStore<String, TenantTireBenefitValueEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantTireBenefitValueEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantTireBenefitValueEntity> = TenantTireBenefitValueEntity::class

    override fun getRepository(): TenantTireBenefitValueRepository {
        return benefitValueRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateTenantTireBenefitValueDTO): TenantTireBenefitValueEntity {
        if (dto.featureValue.isBlank()) {
            throw BusinessException("featureValue must not be blank")
        }

        val entity = TenantTireBenefitValueEntity(
            id = snowIdGenerator.nextId(),
            tireTypeId = dto.tireTypeId,
            featureId = dto.featureId,
            featureValue = dto.featureValue,
        ).apply { newEntity() }
        return benefitValueRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create tenant benefit value")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateTenantTireBenefitValueDTO, original: TenantTireBenefitValueEntity): TenantTireBenefitValueEntity {
        return original.apply {
            dto.tireTypeId?.let { tireTypeId = it }
            dto.featureId?.let { featureId = it }
            dto.featureValue?.let { featureValue = it }
        }
    }
}
