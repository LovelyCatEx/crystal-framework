package com.lovelycatv.crystalframework.resource.service

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRepository
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.shared.utils.EntityQueryableFieldsResolver
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.toPrettierJSONString
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StorageProviderServiceTest(
    @Autowired private val storageProviderRepository: StorageProviderRepository,
    @Autowired private val snowIdGenerator: SnowIdGenerator,
) : CrystalFrameworkApplicationTests() {

    /**
     * [StorageProviderService] only extends `CachedBaseService` with no custom `create`
     * method, so per the integration-test rules this mock persists via the repository directly.
     */
    suspend fun mockStorageProvider(
        name: String = "TestProvider",
        properties: String = "{\"accessKey\":\"ak\",\"secretKey\":\"sk\"}",
    ): StorageProviderEntity {
        val entity = storageProviderRepository.save(
            StorageProviderEntity(
                id = snowIdGenerator.nextId(),
                name = name,
                type = StorageProviderType.LOCAL_FILE_SYSTEM.typeId,
                baseUrl = "http://localhost",
                properties = properties,
            ).apply { newEntity() }
        ).awaitFirstOrNull() ?: error("Failed to create storage provider")
        println("[mock] StorageProvider: ${entity.toPrettierJSONString()}")
        return entity
    }

    @Test
    fun createStorageProvider() {
        withTransactionalRollback("storage-provider-create") {
            val provider = mockStorageProvider()
            assertNotNull(provider)
            val reloaded = storageProviderRepository.findById(provider.id).awaitFirstOrNull()
            assertNotNull(reloaded)
            assertEquals(provider.name, reloaded.name)
        }
    }

    /**
     * The `properties` column stores OSS/COS accessKey/secretKey credentials and carries
     * `@NotQueryable`, so it must be excluded from the client-facing query allowlist to
     * prevent boolean-blind probing of the credential JSON.
     */
    @Test
    fun propertiesFieldIsNotQueryable() {
        val queryable = EntityQueryableFieldsResolver.resolve(StorageProviderEntity::class)
        assertFalse(
            queryable.contains("properties"),
            "properties (credential JSON) must not be queryable"
        )
    }

    /**
     * Regression guard: adding `@NotQueryable` to `properties` must not accidentally strip
     * legitimate business columns from the allowlist.
     */
    @Test
    fun normalFieldsRemainQueryable() {
        val queryable = EntityQueryableFieldsResolver.resolve(StorageProviderEntity::class)
        assertTrue(queryable.contains("name"), "name must stay queryable")
        assertTrue(queryable.contains("base_url"), "base_url must stay queryable")
        assertTrue(queryable.contains("type"), "type must stay queryable")
        assertTrue(queryable.contains("active"), "active must stay queryable")
    }
}
