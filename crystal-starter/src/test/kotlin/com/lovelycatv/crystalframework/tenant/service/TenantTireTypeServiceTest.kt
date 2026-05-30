package com.lovelycatv.crystalframework.tenant.service

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.toPrettierJSONString
import com.lovelycatv.crystalframework.tenant.entity.TenantTireTypeEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireTypeRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TenantTireTypeServiceTest(
    @Autowired private val tenantTireTypeRepository: TenantTireTypeRepository,
    @Autowired private val snowIdGenerator: SnowIdGenerator,
) : CrystalFrameworkApplicationTests() {

    suspend fun mockTireType(name: String = "TestTire"): TenantTireTypeEntity {
        val entity = tenantTireTypeRepository.save(
            TenantTireTypeEntity(
                id = snowIdGenerator.nextId(),
                name = name,
            ).apply { newEntity() }
        ).awaitFirstOrNull() ?: error("Failed to create tire type")
        println("[mock] TireType: ${entity.toPrettierJSONString()}")
        return entity
    }

    @Test
    fun createTireType() {
        withTransactionalRollback("tire-type-create") {
            val tireType = mockTireType()
            kotlin.test.assertNotNull(tireType)
        }
    }
}
