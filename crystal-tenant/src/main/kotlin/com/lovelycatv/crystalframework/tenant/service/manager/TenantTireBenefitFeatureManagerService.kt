package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitFeatureDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerDeleteTenantTireBenefitFeatureDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerReadTenantTireBenefitFeatureDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerUpdateTenantTireBenefitFeatureDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantTireBenefitFeatureEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitFeatureRepository

interface TenantTireBenefitFeatureManagerService : CachedBaseManagerService<
        TenantTireBenefitFeatureRepository,
        TenantTireBenefitFeatureEntity,
        ManagerCreateTenantTireBenefitFeatureDTO,
        ManagerReadTenantTireBenefitFeatureDTO,
        ManagerUpdateTenantTireBenefitFeatureDTO,
        ManagerDeleteTenantTireBenefitFeatureDTO
> {
    suspend fun findAllFeatures(): List<TenantTireBenefitFeatureEntity>
}
