package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.toPrettierJSONString
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitFeatureRepository
import com.lovelycatv.crystalframework.tenant.constants.TenantBenefit
import com.lovelycatv.crystalframework.tenant.service.TenantBenefitServiceTest
import com.lovelycatv.crystalframework.tenant.service.TenantServiceTest
import com.lovelycatv.crystalframework.tenant.service.TenantTireTypeServiceTest
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitValueManagerService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TenantMemberManagerServiceImplTest(
    @Autowired private val memberManagerService: TenantMemberManagerService,
    @Autowired private val benefitFeatureRepository: TenantTireBenefitFeatureRepository,
    @Autowired private val benefitValueManagerService: TenantTireBenefitValueManagerService,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val tireTypeServiceTest: TenantTireTypeServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val tenantServiceTest: TenantServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val benefitServiceTest: TenantBenefitServiceTest by lazy { getTestClassInstance(applicationContext) }

    suspend fun mockMember(tenantId: Long, userId: Long): TenantMemberEntity {
        val entity = memberManagerService.create(
            ManagerCreateTenantMemberDTO(
                tenantId = tenantId,
                memberUserId = userId,
            )
        )
        println("[mock] Member: ${entity.toPrettierJSONString()}")
        return entity
    }

    private suspend fun setBenefitValue(tireTypeId: Long, featureKey: String, value: String) {
        val feature = benefitFeatureRepository.findByFeatureKey(featureKey).awaitFirstOrNull()
            ?: error("Feature $featureKey not found")
        benefitValueManagerService.create(ManagerCreateTenantTireBenefitValueDTO(tireTypeId, feature.id, value))
    }

    @Test
    fun createMemberWithinLimit() {
        withTransactionalRollback("member-within-limit") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val tenant = tenantServiceTest.mockTenant(owner.id, tireType.id)
            val newUser = tenantServiceTest.mockUser()

            setBenefitValue(tireType.id, TenantBenefit.MEMBER_MAX_COUNT.featureKey, "10")

            val member = memberManagerService.create(
                ManagerCreateTenantMemberDTO(
                    tenantId = tenant.id,
                    memberUserId = newUser.id,
                )
            )
            assertNotNull(member)
        }
    }

    @Test
    fun createMemberExceedsLimit() {
        withTransactionalRollback("member-exceeds-limit") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val tenant = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.MEMBER_MAX_COUNT.featureKey, "0")

            val result = runCatching {
                memberManagerService.create(
                    ManagerCreateTenantMemberDTO(
                        tenantId = tenant.id,
                        memberUserId = owner.id,
                    )
                )
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex)
            assertTrue(ex is BusinessException)
            assertTrue((ex as BusinessException).message!!.contains("limit", ignoreCase = true))
        }
    }

    @Test
    fun createMemberAtLimitThenExceeds() {
        withTransactionalRollback("member-at-limit") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val tenant = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.MEMBER_MAX_COUNT.featureKey, "2")

            val user1 = tenantServiceTest.mockUser()
            val member1 = memberManagerService.create(
                ManagerCreateTenantMemberDTO(tenantId = tenant.id, memberUserId = user1.id)
            )
            assertNotNull(member1)

            val user2 = tenantServiceTest.mockUser()
            val member2 = memberManagerService.create(
                ManagerCreateTenantMemberDTO(tenantId = tenant.id, memberUserId = user2.id)
            )
            assertNotNull(member2)

            val user3 = tenantServiceTest.mockUser()
            val result = runCatching {
                memberManagerService.create(
                    ManagerCreateTenantMemberDTO(tenantId = tenant.id, memberUserId = user3.id)
                )
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException when exceeding member limit")
            assertTrue(ex is BusinessException)
        }
    }
}
