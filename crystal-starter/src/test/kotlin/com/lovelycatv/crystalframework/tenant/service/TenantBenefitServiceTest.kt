package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.TenantBenefitRegistry
import com.lovelycatv.crystalframework.shared.utils.toPrettierJSONString
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitFeatureRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TenantBenefitServiceTest(
    @Autowired private val benefitFeatureRepository: TenantTireBenefitFeatureRepository,
    @Autowired private val tenantBenefitRegistry: TenantBenefitRegistry,
) : CrystalFrameworkApplicationTests() {

    suspend fun ensureBenefitFeaturesExist() {
        val declarations = tenantBenefitRegistry.benefitDeclarations()
        declarations.forEach { decl ->
            val existing = benefitFeatureRepository
                .findByFeatureKey(decl.featureKey)
                .awaitFirstOrNull()
            kotlin.test.assertNotNull(existing) {
                "Tenant benefit feature '$decl.featureKey' is missing from database. " +
                    "TenantBenefitTableDataCheckRunner should have created it at startup."
            }
        }
        println("[verify] BenefitFeatures: ${declarations.size} feature(s) exist: ${declarations.map { it.featureKey }}")
    }

    @Test
    fun allBenefitFeaturesExist() {
        withTransactionalRollback("benefit-features-exist") {
            ensureBenefitFeaturesExist()
            val count = tenantBenefitRegistry.benefitDeclarations().size
            kotlin.test.assertTrue(count > 0)
        }
    }
}
