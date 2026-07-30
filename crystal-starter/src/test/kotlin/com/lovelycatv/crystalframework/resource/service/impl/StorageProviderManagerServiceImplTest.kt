package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerCreateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerUpdateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.StorageProviderManagerService
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.shared.utils.toPrettierJSONString
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class StorageProviderManagerServiceImplTest(
    @Autowired private val storageProviderManagerService: StorageProviderManagerService,
) : CrystalFrameworkApplicationTests() {

    suspend fun mockStorageProvider(
        name: String = "test-provider-${UUID.randomUUID()}",
        active: Boolean = true,
    ): StorageProviderEntity {
        val created = storageProviderManagerService.create(
            ManagerCreateStorageProviderDTO(
                name = name,
                description = null,
                type = StorageProviderType.LOCAL_FILE_SYSTEM.typeId,
                baseUrl = "http://localhost/files",
                properties = "{}",
            )
        )
        if (active) return created
        return storageProviderManagerService.update(
            ManagerUpdateStorageProviderDTO(id = created.id, active = false)
        ) ?: error("Failed to disable mocked storage provider")
    }

    @Test
    fun createStorageProvider() {
        withTransactionalRollback("storage-provider-create") {
            val provider = mockStorageProvider()
            assertNotNull(provider)
            println(provider.toPrettierJSONString())
        }
    }
}
