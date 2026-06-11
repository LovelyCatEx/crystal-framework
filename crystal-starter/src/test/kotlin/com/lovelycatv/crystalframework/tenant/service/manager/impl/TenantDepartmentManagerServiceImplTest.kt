package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerCreateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitFeatureRepository
import com.lovelycatv.crystalframework.tenant.constants.TenantBenefit
import com.lovelycatv.crystalframework.tenant.service.TenantBenefitServiceTest
import com.lovelycatv.crystalframework.tenant.service.TenantServiceTest
import com.lovelycatv.crystalframework.tenant.service.TenantTireTypeServiceTest
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitValueManagerService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TenantDepartmentManagerServiceImplTest(
    @Autowired private val departmentManagerService: TenantDepartmentManagerService,
    @Autowired private val benefitFeatureRepository: TenantTireBenefitFeatureRepository,
    @Autowired private val benefitValueManagerService: TenantTireBenefitValueManagerService,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val tireTypeServiceTest: TenantTireTypeServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val tenantServiceTest: TenantServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val benefitServiceTest: TenantBenefitServiceTest by lazy { getTestClassInstance(applicationContext) }

    private suspend fun setBenefitValue(tireTypeId: Long, featureKey: String, value: String) {
        val feature = benefitFeatureRepository.findByFeatureKey(featureKey).awaitFirstOrNull()
            ?: error("Feature $featureKey not found")
        benefitValueManagerService.create(ManagerCreateTenantTireBenefitValueDTO(tireTypeId, feature.id, value))
    }

    @Test
    fun createDepartmentWithinLimit() {
        withTransactionalRollback("dept-within-limit") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val (tenant) = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.DEPARTMENT_MAX_COUNT.featureKey, "10")

            val dept = departmentManagerService.create(
                ManagerCreateTenantDepartmentDTO(
                    tenantId = tenant.id,
                    name = "Engineering",
                )
            )
            assertNotNull(dept)
        }
    }

    @Test
    fun createDepartmentExceedsLimit() {
        withTransactionalRollback("dept-exceeds-limit") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val (tenant) = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.DEPARTMENT_MAX_COUNT.featureKey, "0")

            val result = runCatching {
                departmentManagerService.create(
                    ManagerCreateTenantDepartmentDTO(
                        tenantId = tenant.id,
                        name = "Engineering",
                    )
                )
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex)
            assertTrue(ex is BusinessException)
            assertTrue((ex as BusinessException).message!!.contains("limit"))
        }
    }

    @Test
    fun createDepartmentAtLimitThenExceeds() {
        withTransactionalRollback("dept-at-limit") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val (tenant) = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.DEPARTMENT_MAX_COUNT.featureKey, "2")

            val dept1 = departmentManagerService.create(
                ManagerCreateTenantDepartmentDTO(tenantId = tenant.id, name = "Engineering")
            )
            assertNotNull(dept1)

            val dept2 = departmentManagerService.create(
                ManagerCreateTenantDepartmentDTO(tenantId = tenant.id, name = "Marketing")
            )
            assertNotNull(dept2)

            val result = runCatching {
                departmentManagerService.create(
                    ManagerCreateTenantDepartmentDTO(tenantId = tenant.id, name = "Sales")
                )
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException when exceeding department limit")
            assertTrue(ex is BusinessException)
        }
    }
}
