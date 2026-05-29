package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitFeatureRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitValueRepository
import com.lovelycatv.crystalframework.tenant.service.TenantBenefitService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class TenantBenefitServiceImpl(
    private val benefitFeatureRepository: TenantTireBenefitFeatureRepository,
    private val benefitValueRepository: TenantTireBenefitValueRepository,
) : TenantBenefitService {

    override suspend fun getBenefitValue(tireTypeId: Long, featureKey: String): String? {
        val feature = benefitFeatureRepository.findByFeatureKey(featureKey).awaitFirstOrNull()
            ?: return null

        val value = benefitValueRepository
            .findByTireTypeIdAndFeatureId(tireTypeId, feature.id)
            .awaitFirstOrNull()

        return value?.featureValue ?: feature.defaultValue
    }

    override suspend fun hasBenefit(tireTypeId: Long, featureKey: String): Boolean {
        return getBenefitValue(tireTypeId, featureKey)?.toBoolean() == true
    }

    override suspend fun getBenefitLimit(tireTypeId: Long, featureKey: String, defaultLimit: Long): Long {
        val value = getBenefitValue(tireTypeId, featureKey)
            ?: return defaultLimit
        return value.toLongOrNull() ?: defaultLimit
    }

    override suspend fun getAllBenefitsForTireType(tireTypeId: Long): Map<String, String> {
        val features = benefitFeatureRepository.findAll().collectList().awaitFirstOrNull() ?: emptyList()

        val valueMap = benefitValueRepository
            .findByTireTypeId(tireTypeId)
            .collectList()
            .awaitFirstOrNull()
            ?.associateBy { it.featureId }
            ?: emptyMap()

        return features.associate { feature ->
            val value = valueMap[feature.id]?.featureValue ?: feature.defaultValue
            feature.featureKey to (value ?: "")
        }
    }
}
