package com.lovelycatv.crystalframework.resource.service.manager.impl

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerReadStorageProviderDTO
import com.lovelycatv.crystalframework.resource.service.StorageProviderServiceTest
import com.lovelycatv.crystalframework.resource.service.manager.StorageProviderManagerService
import com.lovelycatv.crystalframework.shared.database.ConditionNode
import com.lovelycatv.crystalframework.shared.database.QueryOperator
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StorageProviderManagerServiceImplTest(
    @Autowired private val storageProviderManagerService: StorageProviderManagerService,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val storageProviderServiceTest: StorageProviderServiceTest by lazy {
        getTestClassInstance(applicationContext)
    }

    /**
     * Success case: querying by a whitelisted column (`name`) returns the persisted record.
     */
    @Test
    fun queryByAllowedFieldSucceeds() {
        withTransactionalRollback("storage-provider-query-allowed") {
            val provider = storageProviderServiceTest.mockStorageProvider(name = "QueryableProvider")

            val result = storageProviderManagerService.query(
                ManagerReadStorageProviderDTO(
                    page = 1,
                    pageSize = 20,
                    query = ConditionNode(
                        field = "name",
                        operator = QueryOperator.EQ,
                        value = provider.name,
                    ),
                )
            )

            assertTrue(result.total >= 1, "expected at least one record for name=${provider.name}")
            assertTrue(
                result.records.any { it.name == provider.name },
                "query result must contain the persisted provider"
            )
        }
    }

    /**
     * Failure case: querying by `properties` (`@NotQueryable`, stores OSS/COS credentials)
     * must be rejected before any SQL runs, preventing boolean-blind probing of the secret.
     */
    @Test
    fun queryByNotQueryableFieldThrows() {
        withTransactionalRollback("storage-provider-query-forbidden") {
            val result = runCatching {
                storageProviderManagerService.query(
                    ManagerReadStorageProviderDTO(
                        page = 1,
                        pageSize = 20,
                        query = ConditionNode(
                            field = "properties",
                            operator = QueryOperator.EQ,
                            value = "anything",
                        ),
                    )
                )
            }

            val ex = result.exceptionOrNull()
            assertNotNull(ex, "querying a @NotQueryable field must fail")
            assertTrue(ex is BusinessException, "expected BusinessException but was ${ex::class.simpleName}")
            assertTrue(
                ex.message!!.contains("properties"),
                "error message must name the forbidden field, was: ${ex.message}"
            )
        }
    }
}
