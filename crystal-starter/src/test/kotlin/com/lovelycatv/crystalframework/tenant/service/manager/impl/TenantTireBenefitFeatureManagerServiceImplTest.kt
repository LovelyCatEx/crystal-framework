package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.toPrettierJSONString
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitFeatureDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerUpdateTenantTireBenefitFeatureDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantTireBenefitFeatureEntity
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitFeatureManagerService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TenantTireBenefitFeatureManagerServiceImplTest(
    @Autowired private val featureManagerService: TenantTireBenefitFeatureManagerService,
) : CrystalFrameworkApplicationTests() {

    private fun nextKey(): String = "test.feature.${java.util.UUID.randomUUID().toString().substring(0, 8)}"

    suspend fun mockFeature(
        featureKey: String = nextKey(),
        name: String = "TestFeature",
        featureType: Int = 0,
        defaultValue: String? = null,
    ): TenantTireBenefitFeatureEntity {
        val entity = featureManagerService.create(
            ManagerCreateTenantTireBenefitFeatureDTO(
                featureKey = featureKey,
                name = name,
                description = null,
                featureType = featureType,
                defaultValue = defaultValue,
            )
        )
        println("[mock] Feature: ${entity.toPrettierJSONString()}")
        return entity
    }

    // ── BOOLEAN defaultValue ──

    @Test
    fun createBooleanWithValidDefault() {
        withTransactionalRollback("feature-create-bool-valid") {
            val feature = mockFeature(featureType = 0, defaultValue = "false")
            assertNotNull(feature)
        }
    }

    @Test
    fun createBooleanWithInvalidDefault() {
        withTransactionalRollback("feature-create-bool-invalid") {
            val result = runCatching {
                mockFeature(featureType = 0, defaultValue = "yes")
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException for invalid BOOLEAN defaultValue")
            assertTrue(ex is BusinessException)
            assertTrue((ex as BusinessException).message!!.contains("BOOLEAN"))
        }
    }

    // ── LIMIT defaultValue ──

    @Test
    fun createLimitWithValidDefault() {
        withTransactionalRollback("feature-create-limit-valid") {
            val feature = mockFeature(featureType = 1, defaultValue = "0")
            assertNotNull(feature)
        }
    }

    @Test
    fun createLimitWithNonIntegerDefault() {
        withTransactionalRollback("feature-create-limit-nonInt") {
            val result = runCatching {
                mockFeature(featureType = 1, defaultValue = "not-a-number")
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException for non-integer LIMIT defaultValue")
            assertTrue(ex is BusinessException)
            assertTrue((ex as BusinessException).message!!.contains("integer"))
        }
    }

    @Test
    fun createLimitWithNegativeDefault() {
        withTransactionalRollback("feature-create-limit-negative") {
            val result = runCatching {
                mockFeature(featureType = 1, defaultValue = "-5")
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException for negative LIMIT defaultValue")
            assertTrue(ex is BusinessException)
            assertTrue((ex as BusinessException).message!!.contains("non-negative"))
        }
    }

    // ── ENUM defaultValue ──

    @Test
    fun createEnumWithValidOptions() {
        withTransactionalRollback("feature-create-enum-valid") {
            val feature = mockFeature(featureType = 2, defaultValue = "A,B,C")
            assertNotNull(feature)
        }
    }

    @Test
    fun createEnumWithBlankDefaultIsSkippedByGuard() {
        withTransactionalRollback("feature-create-enum-blank") {
            // Note: current featureService.create guard skips validation when
            // defaultValue is blank (isNotBlank check). The blank value is stored as-is.
            val feature = mockFeature(featureType = 2, defaultValue = "   ")
            assertNotNull(feature)
        }
    }

    @Test
    fun createEnumWithNullDefaultIsAllowed() {
        withTransactionalRollback("feature-create-enum-null") {
            val feature = mockFeature(featureType = 2, defaultValue = null)
            assertNotNull(feature)
        }
    }

    // ── update defaultValue ──

    @Test
    fun updateBooleanToValidDefault() {
        withTransactionalRollback("feature-update-bool-valid") {
            val feature = mockFeature(featureType = 0, defaultValue = "false")

            val dto = ManagerUpdateTenantTireBenefitFeatureDTO(id = feature.id, defaultValue = "true")
            val updated = featureManagerService.update(dto)
            assertNotNull(updated)
        }
    }

    @Test
    fun updateBooleanToInvalidDefault() {
        withTransactionalRollback("feature-update-bool-invalid") {
            val feature = mockFeature(featureType = 0, defaultValue = "false")

            val result = runCatching {
                val dto = ManagerUpdateTenantTireBenefitFeatureDTO(id = feature.id, defaultValue = "invalid")
                featureManagerService.update(dto)
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException when updating BOOLEAN to invalid defaultValue")
            assertTrue(ex is BusinessException)
        }
    }

    @Test
    fun updateFeatureTypeAndDefaultTogether() {
        withTransactionalRollback("feature-update-type-and-default") {
            val feature = mockFeature(featureType = 0, defaultValue = "false")

            val dto = ManagerUpdateTenantTireBenefitFeatureDTO(
                id = feature.id,
                featureType = 1,
                defaultValue = "100",
            )
            val updated = featureManagerService.update(dto)
            assertNotNull(updated)
        }
    }

    @Test
    fun updateFeatureTypeAndDefaultMismatch() {
        withTransactionalRollback("feature-update-type-mismatch") {
            val feature = mockFeature(featureType = 0, defaultValue = "false")

            val result = runCatching {
                val dto = ManagerUpdateTenantTireBenefitFeatureDTO(
                    id = feature.id,
                    featureType = 1,
                    defaultValue = "not-a-number",
                )
                featureManagerService.update(dto)
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException when type and defaultValue mismatch")
            assertTrue(ex is BusinessException)
            assertTrue((ex as BusinessException).message!!.contains("integer"))
        }
    }
}
