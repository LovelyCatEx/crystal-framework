package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.tenant.constants.TenantBenefit
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerCreateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerCreateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerReadTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitFeatureRepository
import com.lovelycatv.crystalframework.tenant.service.TenantBenefitServiceTest
import com.lovelycatv.crystalframework.tenant.service.TenantServiceTest
import com.lovelycatv.crystalframework.tenant.service.TenantTireTypeServiceTest
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentMemberManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitValueManagerService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import kotlin.test.assertEquals

class TenantDepartmentMemberManagerServiceImplTest(
    @Autowired private val departmentManagerService: TenantDepartmentManagerService,
    @Autowired private val departmentMemberManagerService: TenantDepartmentMemberManagerService,
    @Autowired private val benefitFeatureRepository: TenantTireBenefitFeatureRepository,
    @Autowired private val benefitValueManagerService: TenantTireBenefitValueManagerService,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val tireTypeServiceTest: TenantTireTypeServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val tenantServiceTest: TenantServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val benefitServiceTest: TenantBenefitServiceTest by lazy { getTestClassInstance(applicationContext) }

    private suspend fun setDepartmentLimit(tireTypeId: Long) {
        val feature = benefitFeatureRepository.findByFeatureKey(TenantBenefit.DEPARTMENT_MAX_COUNT.featureKey)
            .awaitFirstOrNull()
            ?: error("Department benefit feature not found")
        benefitValueManagerService.create(
            ManagerCreateTenantTireBenefitValueDTO(tireTypeId, feature.id, "2"),
        )
    }

    private suspend fun createRelationFixture(): Pair<TenantDepartmentMemberRelationEntity, TenantDepartmentMemberRelationEntity> {
        benefitServiceTest.ensureBenefitFeaturesExist()
        val tireType = tireTypeServiceTest.mockTireType()
        val owner = tenantServiceTest.mockUser()
        val (tenant, ownerMember) = tenantServiceTest.mockTenant(owner.id, tireType.id)
        setDepartmentLimit(tireType.id)

        val firstDepartment = departmentManagerService.create(
            ManagerCreateTenantDepartmentDTO(tenant.id, "First Department"),
        )
        val secondDepartment = departmentManagerService.create(
            ManagerCreateTenantDepartmentDTO(tenant.id, "Second Department"),
        )
        val firstRelation = departmentMemberManagerService.create(
            ManagerCreateTenantDepartmentMemberDTO(firstDepartment.id, ownerMember.id),
        )
        val secondRelation = departmentMemberManagerService.create(
            ManagerCreateTenantDepartmentMemberDTO(secondDepartment.id, ownerMember.id),
        )
        return firstRelation to secondRelation
    }

    @Test
    fun queryOnlyReturnsRelationsFromRequestedDepartment() {
        withTransactionalRollback("department-member-query-by-department") {
            val (firstRelation, secondRelation) = createRelationFixture()
            val result = departmentMemberManagerService.query(
                ManagerReadTenantDepartmentMemberDTO(
                    page = 1,
                    pageSize = 20,
                    departmentId = firstRelation.departmentId,
                ),
            )

            assertEquals(1, result.total)
            assertEquals(listOf(firstRelation.id), result.records.map { it.id })
            assertEquals(false, result.records.any { it.id == secondRelation.id })
        }
    }

    @Test
    fun queryByIdCannotEscapeRequestedDepartment() {
        withTransactionalRollback("department-member-query-id-by-department") {
            val (firstRelation, secondRelation) = createRelationFixture()
            val foreignResult = departmentMemberManagerService.query(
                ManagerReadTenantDepartmentMemberDTO(
                    page = 1,
                    pageSize = 20,
                    id = secondRelation.id,
                    departmentId = firstRelation.departmentId,
                ),
            )
            val ownResult = departmentMemberManagerService.query(
                ManagerReadTenantDepartmentMemberDTO(
                    page = 1,
                    pageSize = 20,
                    id = firstRelation.id,
                    departmentId = firstRelation.departmentId,
                ),
            )

            assertEquals(0, foreignResult.total)
            assertEquals(emptyList(), foreignResult.records)
            assertEquals(1, ownResult.total)
            assertEquals(listOf(firstRelation.id), ownResult.records.map { it.id })
        }
    }
}
