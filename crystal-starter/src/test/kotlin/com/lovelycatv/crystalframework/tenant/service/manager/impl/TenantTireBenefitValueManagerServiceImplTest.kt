package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.toPrettierJSONString
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerUpdateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantTireBenefitValueEntity
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitValueManagerService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TenantTireBenefitValueManagerServiceImplTest(
    @Autowired private val valueManagerService: TenantTireBenefitValueManagerService,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val featureTest: TenantTireBenefitFeatureManagerServiceImplTest by lazy {
        getTestClassInstance<TenantTireBenefitFeatureManagerServiceImplTest>(applicationContext)
    }

    suspend fun mockValue(
        tireTypeId: Long,
        featureId: Long,
        featureValue: String,
    ): TenantTireBenefitValueEntity {
        val entity = valueManagerService.create(
            ManagerCreateTenantTireBenefitValueDTO(
                tireTypeId = tireTypeId,
                featureId = featureId,
                featureValue = featureValue,
            )
        )
        println("[mock] Value: ${entity.toPrettierJSONString()}")
        return entity
    }

    private suspend fun mockBooleanFeature(defaultValue: String = "false") =
        featureTest.mockFeature(featureType = 0, defaultValue = defaultValue)

    private suspend fun mockLimitFeature(defaultValue: String = "0") =
        featureTest.mockFeature(featureType = 1, defaultValue = defaultValue)

    private suspend fun mockEnumFeature(options: String = "Gold,Silver,Bronze") =
        featureTest.mockFeature(featureType = 2, defaultValue = options)

    // ── BOOLEAN featureValue ──

    @Test
    fun createBooleanValueValid() {
        withTransactionalRollback("value-create-bool-valid") {
            val feature = mockBooleanFeature()
            val entity = mockValue(featureId = feature.id, tireTypeId = 0, featureValue = "true")
            assertNotNull(entity)
        }
    }

    @Test
    fun createBooleanValueInvalid() {
        withTransactionalRollback("value-create-bool-invalid") {
            val feature = mockBooleanFeature()
            val result = runCatching {
                mockValue(featureId = feature.id, tireTypeId = 0, featureValue = "maybe")
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException for invalid BOOLEAN featureValue")
            assertTrue(ex is BusinessException)
            assertTrue((ex as BusinessException).message!!.contains("BOOLEAN"))
        }
    }

    // ── LIMIT featureValue ──

    @Test
    fun createLimitValueValid() {
        withTransactionalRollback("value-create-limit-valid") {
            val feature = mockLimitFeature()
            val entity = mockValue(featureId = feature.id, tireTypeId = 0, featureValue = "50")
            assertNotNull(entity)
        }
    }

    @Test
    fun createLimitValueNonInteger() {
        withTransactionalRollback("value-create-limit-nonInt") {
            val feature = mockLimitFeature()
            val result = runCatching {
                mockValue(featureId = feature.id, tireTypeId = 0, featureValue = "abc")
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException for non-integer LIMIT featureValue")
            assertTrue(ex is BusinessException)
            assertTrue((ex as BusinessException).message!!.contains("integer"))
        }
    }

    @Test
    fun createLimitValueNegative() {
        withTransactionalRollback("value-create-limit-negative") {
            val feature = mockLimitFeature()
            val result = runCatching {
                mockValue(featureId = feature.id, tireTypeId = 0, featureValue = "-1")
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException for negative LIMIT featureValue")
            assertTrue(ex is BusinessException)
            assertTrue((ex as BusinessException).message!!.contains("non-negative"))
        }
    }

    // ── ENUM featureValue ──

    @Test
    fun createEnumValueValidOption() {
        withTransactionalRollback("value-create-enum-valid") {
            val feature = mockEnumFeature("Gold,Silver,Bronze")
            val entity = mockValue(featureId = feature.id, tireTypeId = 0, featureValue = "Silver")
            assertNotNull(entity)
        }
    }

    @Test
    fun createEnumValueNotInOptions() {
        withTransactionalRollback("value-create-enum-notInOptions") {
            val feature = mockEnumFeature("Gold,Silver,Bronze")
            val result = runCatching {
                mockValue(featureId = feature.id, tireTypeId = 0, featureValue = "Platinum")
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException for ENUM featureValue not in options")
            assertTrue(ex is BusinessException)
            assertTrue((ex as BusinessException).message!!.contains("must be one of"))
        }
    }

    @Test
    fun createEnumValueBlank() {
        withTransactionalRollback("value-create-enum-blank") {
            val feature = mockEnumFeature("Gold,Silver,Bronze")
            val result = runCatching {
                mockValue(featureId = feature.id, tireTypeId = 0, featureValue = "   ")
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException for blank featureValue")
            assertTrue(ex is BusinessException)
        }
    }

    // ── update featureValue ──

    @Test
    fun updateLimitValueToValid() {
        withTransactionalRollback("value-update-limit-valid") {
            val feature = mockLimitFeature()
            val entity = mockValue(featureId = feature.id, tireTypeId = 0, featureValue = "10")

            val dto = ManagerUpdateTenantTireBenefitValueDTO(id = entity.id, featureValue = "99")
            val updated = valueManagerService.update(dto)
            assertNotNull(updated)
        }
    }

    @Test
    fun updateLimitValueToInvalid() {
        withTransactionalRollback("value-update-limit-invalid") {
            val feature = mockLimitFeature()
            val entity = mockValue(featureId = feature.id, tireTypeId = 0, featureValue = "10")

            val result = runCatching {
                val dto = ManagerUpdateTenantTireBenefitValueDTO(id = entity.id, featureValue = "not-a-number")
                valueManagerService.update(dto)
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException when updating to invalid featureValue")
            assertTrue(ex is BusinessException)
        }
    }

    @Test
    fun updateEnumValueToDifferentValidOption() {
        withTransactionalRollback("value-update-enum-valid") {
            val feature = mockEnumFeature("Gold,Silver,Bronze")
            val entity = mockValue(featureId = feature.id, tireTypeId = 0, featureValue = "Silver")

            val dto = ManagerUpdateTenantTireBenefitValueDTO(id = entity.id, featureValue = "Bronze")
            val updated = valueManagerService.update(dto)
            assertNotNull(updated)
        }
    }

    @Test
    fun updateEnumValueToInvalidOption() {
        withTransactionalRollback("value-update-enum-invalid") {
            val feature = mockEnumFeature("Gold,Silver,Bronze")
            val entity = mockValue(featureId = feature.id, tireTypeId = 0, featureValue = "Silver")

            val result = runCatching {
                val dto = ManagerUpdateTenantTireBenefitValueDTO(id = entity.id, featureValue = "Platinum")
                valueManagerService.update(dto)
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException when updating ENUM to option not in list")
            assertTrue(ex is BusinessException)
            assertTrue((ex as BusinessException).message!!.contains("must be one of"))
        }
    }
}
