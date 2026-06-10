package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.types.TenantBenefitType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerUpdateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantTireBenefitValueEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitFeatureRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitValueRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitValueManagerService
import com.lovelycatv.crystalframework.tenant.utils.TenantBenefitValidator
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantTireBenefitValueManagerServiceImpl(
    private val benefitValueRepository: TenantTireBenefitValueRepository,
    private val benefitFeatureRepository: TenantTireBenefitFeatureRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : TenantTireBenefitValueManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, TenantTireBenefitValueEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantTireBenefitValueEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantTireBenefitValueEntity> = TenantTireBenefitValueEntity::class

    override fun getRepository(): TenantTireBenefitValueRepository {
        return benefitValueRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateTenantTireBenefitValueDTO): TenantTireBenefitValueEntity {
        if (dto.featureValue.isBlank()) {
            throw BusinessException("featureValue must not be blank")
        }
        validateFeatureValue(dto.featureId, dto.featureValue)

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
        val effectiveFeatureId = dto.featureId ?: original.featureId
        dto.featureValue?.let { validateFeatureValue(effectiveFeatureId, it) }
        return original.apply {
            dto.tireTypeId?.let { tireTypeId = it }
            dto.featureId?.let { featureId = it }
            dto.featureValue?.let { featureValue = it }
        }
    }

    private suspend fun validateFeatureValue(featureId: Long, featureValue: String) {
        val feature = benefitFeatureRepository.findById(featureId).awaitFirstOrNull()
            ?: throw BusinessException("Benefit feature not found: $featureId")

        TenantBenefitValidator.validateByType(feature.featureType, featureValue, "featureValue")

        if (TenantBenefitType.entries.find { it.typeId == feature.featureType } == TenantBenefitType.ENUM) {
            TenantBenefitValidator.validateEnumAllowedValue(feature, featureValue)
        }
    }
}
