package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitFeatureDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerUpdateTenantTireBenefitFeatureDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantTireBenefitFeatureEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitFeatureRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitValueRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitFeatureManagerService
import com.lovelycatv.crystalframework.tenant.utils.TenantBenefitValidator
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantTireBenefitFeatureManagerServiceImpl(
    private val benefitFeatureRepository: TenantTireBenefitFeatureRepository,
    private val benefitValueRepository: TenantTireBenefitValueRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : TenantTireBenefitFeatureManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, TenantTireBenefitFeatureEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantTireBenefitFeatureEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantTireBenefitFeatureEntity> = TenantTireBenefitFeatureEntity::class

    override fun getRepository(): TenantTireBenefitFeatureRepository {
        return benefitFeatureRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateTenantTireBenefitFeatureDTO): TenantTireBenefitFeatureEntity {
        val featureKey = dto.featureKey.trim()
        if (featureKey.isEmpty()) {
            throw BusinessException("featureKey must not be blank")
        }
        if (benefitFeatureRepository.findByFeatureKey(featureKey).awaitFirstOrNull() != null) {
            throw BusinessException("feature $featureKey already exists")
        }

        dto.defaultValue?.let { defaultVal ->
            if (defaultVal.isNotBlank()) {
                TenantBenefitValidator.validateByType(dto.featureType, defaultVal, "defaultValue")
            }
        }

        val entity = TenantTireBenefitFeatureEntity(
            id = snowIdGenerator.nextId(),
            featureKey = featureKey,
            name = dto.name,
            description = dto.description,
            featureType = dto.featureType,
            defaultValue = dto.defaultValue,
        ).apply { newEntity() }
        return benefitFeatureRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create tenant benefit feature")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateTenantTireBenefitFeatureDTO, original: TenantTireBenefitFeatureEntity): TenantTireBenefitFeatureEntity {
        dto.featureKey?.let { newKey ->
            val trimmed = newKey.trim()
            if (trimmed.isEmpty()) {
                throw BusinessException("featureKey must not be blank")
            }
            if (trimmed != original.featureKey) {
                benefitFeatureRepository.findByFeatureKey(trimmed).awaitFirstOrNull()?.let { collision ->
                    if (collision.id != original.id) {
                        throw BusinessException("feature $trimmed already exists")
                    }
                }
                original.featureKey = trimmed
            }
        }
        val effectiveFeatureType = dto.featureType ?: original.featureType
        dto.defaultValue?.let { defaultVal ->
            if (defaultVal.isNotBlank()) {
                TenantBenefitValidator.validateByType(effectiveFeatureType, defaultVal, "defaultValue")
            }
        }
        return original.apply {
            dto.name?.let { name = it }
            dto.description?.let { description = it }
            dto.featureType?.let { featureType = it }
            dto.defaultValue?.let { defaultValue = it }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun batchDelete(ids: List<Long>) {
        benefitValueRepository.deleteByFeatureIdIn(ids).awaitFirstOrNull()
        super.batchDelete(ids)
    }
}
