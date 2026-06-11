package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.toPrettierJSONString
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitFeatureRepository
import com.lovelycatv.crystalframework.tenant.constants.TenantBenefit
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerReadTenantMemberDTO
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
import org.bouncycastle.asn1.x500.style.RFC4519Style.owner
import kotlin.test.assertEquals
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
                    tenantId = tenant.first.id,
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
            val (tenant) = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.MEMBER_MAX_COUNT.featureKey, "0")

            val result = runCatching {
                memberManagerService.create(
                    ManagerCreateTenantMemberDTO(
                        tenantId = tenant.id,
                        memberUserId = tenantServiceTest.mockUser().id,
                    )
                )
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex)
            assertTrue(ex is BusinessException)
            assertTrue(ex.message!!.contains("limit", ignoreCase = true))
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
                ManagerCreateTenantMemberDTO(tenantId = tenant.first.id, memberUserId = user1.id)
            )
            assertNotNull(member1)

            val user2 = tenantServiceTest.mockUser()
            val result = runCatching {
                memberManagerService.create(
                    ManagerCreateTenantMemberDTO(tenantId = tenant.first.id, memberUserId = user2.id)
                )
            }

            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException when exceeding member limit")
            assertTrue(ex is BusinessException)
        }
    }

    @Test
    fun queryMemberInScope() {
        withTransactionalRollback("query-member-in-scope") {
            val tireType = tireTypeServiceTest.mockTireType("testTireType")

            val tenantOwner1 = tenantServiceTest.mockUser()
            val tenant1 = tenantServiceTest.mockTenant(tenantOwner1.id, tireType.id)

            val tenantOwner2 = tenantServiceTest.mockUser()
            val tenant2 = tenantServiceTest.mockTenant(tenantOwner2.id, tireType.id)

            val result1 = memberManagerService.queryVO(ManagerReadTenantMemberDTO(1, 20, tenant1.first.id))
            val result2 = memberManagerService.queryVO(ManagerReadTenantMemberDTO(1, 20, tenant2.first.id))

            assertEquals(1, result1.records.size)
            assertEquals(1, result2.records.size)

            assertEquals(tenantOwner1.id, result1.records[0].memberUserId)
            assertEquals(tenantOwner2.id, result2.records[0].memberUserId)
        }
    }
}
