package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.types.tenant.TenantStatus
import com.lovelycatv.crystalframework.shared.utils.toPrettierJSONString
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerReadTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerCreateTenantDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.service.manager.TenantManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMemberManagerService
import com.lovelycatv.crystalframework.tenant.service.manager.impl.TenantMemberManagerServiceImplTest
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.service.UserServiceTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

class TenantServiceTest(
    @Autowired private val tenantManagerService: TenantManagerService,
    @Autowired private val tenantMemberManagerService: TenantMemberManagerService,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val userServiceTest: UserServiceTest by lazy {
        getTestClassInstance(applicationContext)
    }

    suspend fun mockTenant(ownerUserId: Long, tireTypeId: Long): Pair<TenantEntity, TenantMemberEntity> {
        val entity = tenantManagerService.create(
            ManagerCreateTenantDTO(
                name = "TestTenant",
                description = "TestTenant description",
                ownerUserId = ownerUserId,
                tireTypeId = tireTypeId,
                subscribedTime = System.currentTimeMillis(),
                expiresTime = System.currentTimeMillis() + 3600000L,
                contactName = "TestTenant contactName",
                contactEmail = "test@example.com",
                contactPhone = "",
                settings = null,
                status = TenantStatus.ACTIVE.typeId,
                address = "TestTenant address",
            )
        )

        println("[mock] Tenant: ${entity.toPrettierJSONString()}")
        return entity to tenantMemberManagerService
            .query(ManagerReadTenantMemberDTO(1, 20, entity.id))
            .records
            .first { it.memberUserId == ownerUserId }
    }

    suspend fun mockUser(): UserEntity = userServiceTest.mockRegisteredUser()

    suspend fun mockTenantWithMembers(n: Int): Pair<Pair<TenantEntity, TenantMemberEntity>, List<TenantMemberEntity>> {
        val tireTypeServiceTest = getTestClassInstance<TenantTireTypeServiceTest>(applicationContext)
        val memberServiceTest = getTestClassInstance<TenantMemberManagerServiceImplTest>(applicationContext)
        val tireType = tireTypeServiceTest.mockTireType()
        val owner = mockUser()
        val tenant = mockTenant(owner.id, tireType.id)
        val members = (1..n).map {
            val user = mockUser()
            memberServiceTest.mockMember(tenant.first.id, user.id)
        }
        println("[mock] TenantWithMembers: id=${tenant.first.id}, memberCount=${members.size + 1}")
        return tenant to members
    }

    @Test
    fun createTenantWithMembers() {
        withTransactionalRollback("tenant-with-members") {
            val (tenant, members) = mockTenantWithMembers(5)
            kotlin.test.assertNotNull(tenant)
            kotlin.test.assertEquals(5, members.size)
        }
    }

    @Test
    fun createTenant() {
        withTransactionalRollback("tenant-create") {
            val user = mockUser()
            val tireTypeServiceTest = getTestClassInstance<TenantTireTypeServiceTest>(applicationContext)
            val tireType = tireTypeServiceTest.mockTireType()
            val tenant = mockTenant(user.id, tireType.id)
            kotlin.test.assertNotNull(tenant)
        }
    }
}
