package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerCreateInvitationDTO
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitFeatureRepository
import com.lovelycatv.crystalframework.tenant.constants.TenantBenefit
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerReadTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.service.TenantBenefitServiceTest
import com.lovelycatv.crystalframework.tenant.service.TenantServiceTest
import com.lovelycatv.crystalframework.tenant.service.TenantTireTypeServiceTest
import com.lovelycatv.crystalframework.tenant.service.manager.TenantInvitationManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitValueManagerService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TenantInvitationManagerServiceImplTest(
    @Autowired private val tenantMemberManagerService: TenantMemberManagerService,
    @Autowired private val invitationManagerService: TenantInvitationManagerService,
    @Autowired private val benefitFeatureRepository: TenantTireBenefitFeatureRepository,
    @Autowired private val benefitValueManagerService: TenantTireBenefitValueManagerService,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val tireTypeServiceTest: TenantTireTypeServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val tenantServiceTest: TenantServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val memberServiceTest: TenantMemberManagerServiceImplTest by lazy { getTestClassInstance(applicationContext) }
    private val benefitServiceTest: TenantBenefitServiceTest by lazy { getTestClassInstance(applicationContext) }

    private suspend fun setBenefitValue(tireTypeId: Long, featureKey: String, value: String) {
        val feature = benefitFeatureRepository.findByFeatureKey(featureKey).awaitFirstOrNull()
            ?: error("Feature $featureKey not found")
        benefitValueManagerService.create(ManagerCreateTenantTireBenefitValueDTO(tireTypeId, feature.id, value))
    }

    @Test
    fun createInvitationWithinTotalLimit() {
        withTransactionalRollback("invitation-within-total") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val (tenant, member) = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.INVITATION_MAX_COUNT.featureKey, "10")
            setBenefitValue(tireType.id, TenantBenefit.INVITATION_PER_DAY_COUNT.featureKey, "10")
            setBenefitValue(tireType.id, TenantBenefit.INVITATION_PER_CODE_USAGE_LIMIT.featureKey, "5")
            setBenefitValue(tireType.id, TenantBenefit.INVITATION_MAX_VALIDITY_DAYS.featureKey, "30")

            val invitation = invitationManagerService.create(
                ManagerCreateInvitationDTO(
                    tenantId = tenant.id,
                    creatorMemberId = member.id,
                    invitationCount = 1,
                )
            )
            assertNotNull(invitation)
        }
    }

    @Test
    fun createInvitationExceedsTotalLimit() {
        withTransactionalRollback("invitation-exceeds-total") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val (tenant, member) = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.INVITATION_MAX_COUNT.featureKey, "0")

            val result = runCatching {
                invitationManagerService.create(
                    ManagerCreateInvitationDTO(
                        tenantId = tenant.id,
                        creatorMemberId = member.id,
                        invitationCount = 1,
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
    fun createInvitationExceedsDailyLimit() {
        withTransactionalRollback("invitation-exceeds-daily") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val (tenant, member) = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.INVITATION_PER_DAY_COUNT.featureKey, "0")

            val result = runCatching {
                invitationManagerService.create(
                    ManagerCreateInvitationDTO(
                        tenantId = tenant.id,
                        creatorMemberId = member.id,
                        invitationCount = 1,
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
    fun createInvitationRespectsDailyLimit() {
        withTransactionalRollback("invitation-respects-daily") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val (tenant, member) = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.INVITATION_PER_DAY_COUNT.featureKey, "2")
            setBenefitValue(tireType.id, TenantBenefit.INVITATION_MAX_COUNT.featureKey, "10")

            val i1 = invitationManagerService.create(
                ManagerCreateInvitationDTO(tenantId = tenant.id, creatorMemberId = member.id, invitationCount = 1)
            )
            assertNotNull(i1)

            val i2 = invitationManagerService.create(
                ManagerCreateInvitationDTO(tenantId = tenant.id, creatorMemberId = member.id, invitationCount = 1)
            )
            assertNotNull(i2)

            val result = runCatching {
                invitationManagerService.create(
                    ManagerCreateInvitationDTO(tenantId = tenant.id, creatorMemberId = member.id, invitationCount = 1)
                )
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex, "Expected BusinessException when exceeding daily limit")
            assertTrue(ex is BusinessException)
        }
    }

    @Test
    fun createInvitationExceedsPerCodeUsageLimit() {
        withTransactionalRollback("invitation-exceeds-perCode") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val (tenant, member) = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.INVITATION_PER_CODE_USAGE_LIMIT.featureKey, "1")

            val result = runCatching {
                invitationManagerService.create(
                    ManagerCreateInvitationDTO(
                        tenantId = tenant.id,
                        creatorMemberId = member.id,
                        invitationCount = 5,
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
    fun createInvitationExceedsMaxValidityDays() {
        withTransactionalRollback("invitation-exceeds-validity") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val (tenant, member) = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.INVITATION_MAX_VALIDITY_DAYS.featureKey, "1")

            val farFuture = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L

            val result = runCatching {
                invitationManagerService.create(
                    ManagerCreateInvitationDTO(
                        tenantId = tenant.id,
                        creatorMemberId = member.id,
                        invitationCount = 1,
                        expiresTime = farFuture,
                    )
                )
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex)
            assertTrue(ex is BusinessException)
            assertTrue((ex as BusinessException).message!!.contains("validity"))
        }
    }

    @Test
    fun createInvitationFailsWhenInvitationDisabled() {
        withTransactionalRollback("invitation-enabled-false") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val (tenant, member) = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.INVITATION_ENABLED.featureKey, "false")
            setBenefitValue(tireType.id, TenantBenefit.INVITATION_MAX_COUNT.featureKey, "10")
            setBenefitValue(tireType.id, TenantBenefit.INVITATION_PER_DAY_COUNT.featureKey, "10")
            setBenefitValue(tireType.id, TenantBenefit.INVITATION_PER_CODE_USAGE_LIMIT.featureKey, "5")
            setBenefitValue(tireType.id, TenantBenefit.INVITATION_MAX_VALIDITY_DAYS.featureKey, "30")

            val result = runCatching {
                invitationManagerService.create(
                    ManagerCreateInvitationDTO(
                        tenantId = tenant.id,
                        creatorMemberId = member.id,
                        invitationCount = 1,
                    )
                )
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex)
            assertTrue(ex is BusinessException)
        }
    }

    @Test
    fun createInvitationSucceedsWhenInvitationEnabledByDefault() {
        withTransactionalRollback("invitation-enabled-default") {
            benefitServiceTest.ensureBenefitFeaturesExist()
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val (tenant, member) = tenantServiceTest.mockTenant(owner.id, tireType.id)

            setBenefitValue(tireType.id, TenantBenefit.INVITATION_MAX_COUNT.featureKey, "10")
            setBenefitValue(tireType.id, TenantBenefit.INVITATION_PER_DAY_COUNT.featureKey, "10")
            setBenefitValue(tireType.id, TenantBenefit.INVITATION_PER_CODE_USAGE_LIMIT.featureKey, "5")
            setBenefitValue(tireType.id, TenantBenefit.INVITATION_MAX_VALIDITY_DAYS.featureKey, "30")

            val invitation = invitationManagerService.create(
                ManagerCreateInvitationDTO(
                    tenantId = tenant.id,
                    creatorMemberId = member.id,
                    invitationCount = 1,
                )
            )
            assertNotNull(invitation)
        }
    }
}
