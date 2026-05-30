package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.toPrettierJSONString
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRepository
import com.lovelycatv.crystalframework.tenant.service.manager.impl.TenantMemberManagerServiceImplTest
import com.lovelycatv.crystalframework.tenant.types.TenantStatus
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.service.UserServiceTest
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

class TenantServiceTest(
    @Autowired private val tenantRepository: TenantRepository,
    @Autowired private val snowIdGenerator: SnowIdGenerator,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val userServiceTest: UserServiceTest by lazy {
        getTestClassInstance(applicationContext)
    }

    suspend fun mockTenant(ownerUserId: Long, tireTypeId: Long): TenantEntity {
        val entity = tenantRepository.save(
            TenantEntity(
                id = snowIdGenerator.nextId(),
                ownerUserId = ownerUserId,
                name = "TestTenant",
                status = TenantStatus.ACTIVE.ordinal,
                tireTypeId = tireTypeId,
            ).apply { newEntity() }
        ).awaitFirstOrNull() ?: error("Failed to create tenant")
        println("[mock] Tenant: ${entity.toPrettierJSONString()}")
        return entity
    }

    suspend fun mockUser(): UserEntity = userServiceTest.mockRegisteredUser()

    suspend fun mockTenantWithMembers(n: Int): Pair<TenantEntity, List<TenantMemberEntity>> {
        val tireTypeServiceTest = getTestClassInstance<TenantTireTypeServiceTest>(applicationContext)
        val memberServiceTest = getTestClassInstance<TenantMemberManagerServiceImplTest>(applicationContext)
        val tireType = tireTypeServiceTest.mockTireType()
        val owner = mockUser()
        val tenant = mockTenant(owner.id, tireType.id)
        val members = (1..n).map {
            val user = mockUser()
            memberServiceTest.mockMember(tenant.id, user.id)
        }
        println("[mock] TenantWithMembers: id=${tenant.id}, memberCount=${members.size}")
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
