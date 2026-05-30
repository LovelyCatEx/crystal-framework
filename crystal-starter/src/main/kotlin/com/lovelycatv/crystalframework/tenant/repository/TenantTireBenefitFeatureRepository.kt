package com.lovelycatv.crystalframework.tenant.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.tenant.entity.TenantTireBenefitFeatureEntity
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TenantTireBenefitFeatureRepository : BaseRepository<TenantTireBenefitFeatureEntity> {
    fun findByFeatureKey(featureKey: String): Mono<TenantTireBenefitFeatureEntity>
}
