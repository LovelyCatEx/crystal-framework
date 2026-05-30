package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.tenant.constants.TenantBenefit
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitFeatureRepository
import com.lovelycatv.crystalframework.tenant.service.TenantBenefitService
import com.lovelycatv.crystalframework.tenant.service.TenantBenefitServiceTest
import com.lovelycatv.crystalframework.tenant.service.TenantTireTypeServiceTest
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitValueManagerService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TenantBenefitServiceImplTest(
    @Autowired private val tenantBenefitService: TenantBenefitService,
    @Autowired private val benefitFeatureRepository: TenantTireBenefitFeatureRepository,
    @Autowired private val benefitValueManagerService: TenantTireBenefitValueManagerService,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val tireTypeServiceTest: TenantTireTypeServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val benefitServiceTest: TenantBenefitServiceTest by lazy { getTestClassInstance(applicationContext) }

    private suspend fun setBenefitValue(tireTypeId: Long, featureKey: String, value: String) {
        val feature = benefitFeatureRepository.findByFeatureKey(featureKey).awaitFirstOrNull()
            ?: error("Feature $featureKey not found")
        benefitValueManagerService.create(ManagerCreateTenantTireBenefitValueDTO(tireTypeId, feature.id, value))
    }

    @Test
    fun getBenefitValueReturnsSetValue() {
        withTransactionalRollback("getBenefitValue-set") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()

            setBenefitValue(tireType.id, TenantBenefit.MEMBER_MAX_COUNT.featureKey, "50")

            val result = tenantBenefitService.getBenefitValue(tireType.id, TenantBenefit.MEMBER_MAX_COUNT.featureKey)
            assertEquals("50", result)
        }
    }

    @Test
    fun getBenefitValueReturnsDefaultWhenNoValueSet() {
        withTransactionalRollback("getBenefitValue-default") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()

            val result = tenantBenefitService.getBenefitValue(tireType.id, TenantBenefit.INVITATION_MAX_COUNT.featureKey)
            assertEquals(TenantBenefit.INVITATION_MAX_COUNT.defaultValue, result)
        }
    }

    @Test
    fun getBenefitValueReturnsNullForUnknownFeature() {
        withTransactionalRollback("getBenefitValue-unknown") {
            val tireType = tireTypeServiceTest.mockTireType()

            assertNull(tenantBenefitService.getBenefitValue(tireType.id, "nonexistent"))
        }
    }

    @Test
    fun hasBenefitReturnsFalseByDefaultForBooleanFeature() {
        withTransactionalRollback("hasBenefit-default") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()

            assertFalse(tenantBenefitService.hasBenefit(tireType.id, TenantBenefit.EXPORT_DATA_ENABLED.featureKey))
        }
    }

    @Test
    fun hasBenefitReturnsTrueWhenSetToTrue() {
        withTransactionalRollback("hasBenefit-true") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()

            setBenefitValue(tireType.id, TenantBenefit.EXPORT_DATA_ENABLED.featureKey, "true")

            assertTrue(tenantBenefitService.hasBenefit(tireType.id, TenantBenefit.EXPORT_DATA_ENABLED.featureKey))
        }
    }

    @Test
    fun getBenefitLimitReturnsDefaultWhenNoValueSet() {
        withTransactionalRollback("getBenefitLimit-default") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()

            val limit = tenantBenefitService.getBenefitLimit(tireType.id, TenantBenefit.INVITATION_MAX_COUNT.featureKey)
            assertEquals(10L, limit)
        }
    }

    @Test
    fun getBenefitLimitReturnsSetValue() {
        withTransactionalRollback("getBenefitLimit-set") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()

            setBenefitValue(tireType.id, TenantBenefit.INVITATION_MAX_COUNT.featureKey, "25")

            assertEquals(25L, tenantBenefitService.getBenefitLimit(tireType.id, TenantBenefit.INVITATION_MAX_COUNT.featureKey))
        }
    }

    @Test
    fun getBenefitLimitUsesCustomDefaultForUnknownFeature() {
        withTransactionalRollback("getBenefitLimit-customDefault") {
            val tireType = tireTypeServiceTest.mockTireType()

            val limit = tenantBenefitService.getBenefitLimit(tireType.id, "nonexistent", 99L)
            assertEquals(99L, limit)
        }
    }

    @Test
    fun getAllBenefitsForTireTypeReturnsAllFeatures() {
        withTransactionalRollback("getAllBenefits") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()

            val allBenefits = tenantBenefitService.getAllBenefitsForTireType(tireType.id)

            assertNotNull(allBenefits)
            assertTrue(allBenefits.isNotEmpty())
            assertEquals(
                TenantBenefit.INVITATION_MAX_COUNT.defaultValue,
                allBenefits[TenantBenefit.INVITATION_MAX_COUNT.featureKey]
            )
            assertEquals(
                TenantBenefit.MEMBER_MAX_COUNT.defaultValue,
                allBenefits[TenantBenefit.MEMBER_MAX_COUNT.featureKey]
            )
            assertEquals(
                TenantBenefit.DEPARTMENT_MAX_COUNT.defaultValue,
                allBenefits[TenantBenefit.DEPARTMENT_MAX_COUNT.featureKey]
            )
        }
    }

    @Test
    fun getAllBenefitsForTireTypeReturnsCustomValues() {
        withTransactionalRollback("getAllBenefits-custom") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()

            setBenefitValue(tireType.id, TenantBenefit.INVITATION_MAX_COUNT.featureKey, "100")
            setBenefitValue(tireType.id, TenantBenefit.MEMBER_MAX_COUNT.featureKey, "50")

            val allBenefits = tenantBenefitService.getAllBenefitsForTireType(tireType.id)
            assertEquals("100", allBenefits[TenantBenefit.INVITATION_MAX_COUNT.featureKey])
            assertEquals("50", allBenefits[TenantBenefit.MEMBER_MAX_COUNT.featureKey])
        }
    }

    @Test
    fun getAllBenefitsForTireTypeReturnsEmptyMapForUnknownTireType() {
        withTransactionalRollback("getAllBenefits-unknown") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()

            val allBenefits = tenantBenefitService.getAllBenefitsForTireType(tireType.id)
            assertNotNull(allBenefits)
            assertTrue(allBenefits.isNotEmpty())
        }
    }
}
