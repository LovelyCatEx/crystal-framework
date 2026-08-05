package com.lovelycatv.crystalframework.database

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.lockRowForUpdate
import com.lovelycatv.crystalframework.tenant.service.TenantServiceTest
import com.lovelycatv.crystalframework.tenant.service.TenantTireTypeServiceTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Regression guard for M-12: the member / invitation `create` count-then-insert quota checks were
 * bypassable by concurrent transactions. The fix serializes same-tenant writes by taking a
 * transaction-scoped `FOR UPDATE` lock on the tenant row via
 * [lockRowForUpdate][com.lovelycatv.crystalframework.shared.utils.lockRowForUpdate].
 *
 * These tests exercise that lock primitive directly against the real reactive stack. A true two-writer
 * race is not reproduced here because the shared test database is not ephemeral and the winning writes
 * would commit outside the rollback boundary; instead we assert the primitive's behavioural contract —
 * (a) locking an existing anchor row succeeds and (b) locking a non-existent row is a safe no-op — which
 * is exactly what the serialization in the two `create` paths relies on.
 */
class TenantQuotaLockIntegrationTest(
    @Autowired private val entityTemplate: R2dbcEntityTemplate,
    @Autowired private val snowIdGenerator: SnowIdGenerator,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val tireTypeServiceTest: TenantTireTypeServiceTest by lazy { getTestClassInstance(applicationContext) }
    private val tenantServiceTest: TenantServiceTest by lazy { getTestClassInstance(applicationContext) }

    @Test
    fun locksExistingTenantRow() {
        withTransactionalRollback("quota-lock-existing-tenant") {
            val tireType = tireTypeServiceTest.mockTireType()
            val owner = tenantServiceTest.mockUser()
            val (tenant) = tenantServiceTest.mockTenant(owner.id, tireType.id)

            // Locking the live tenant row inside the transaction must return that row's id. The global
            // soft-delete interceptor rewrites this to `... WHERE id = :id AND deleted_time IS NULL FOR UPDATE`;
            // asserting the returned id equals the tenant proves a real row was locked — i.e. the injected
            // predicate is `IS NULL` (matches live rows), not the inverted `IS NOT NULL` (would lock nothing).
            val locked = entityTemplate.lockRowForUpdate(TableConstants.TABLE_TENANTS, tenant.id)
            assertEquals(tenant.id, locked, "FOR UPDATE must lock the live tenant row and return its id")
        }
    }

    @Test
    fun lockingMissingRowIsNoOp() {
        withTransactionalRollback("quota-lock-missing-row") {
            // No row matches this id: the FOR UPDATE SELECT returns nothing, so the primitive must complete
            // without throwing and return null, letting callers lock unconditionally before their quota check.
            val locked = entityTemplate.lockRowForUpdate(TableConstants.TABLE_TENANTS, snowIdGenerator.nextId())
            assertNull(locked, "locking a non-existent row must be a no-op that returns null")
        }
    }
}
