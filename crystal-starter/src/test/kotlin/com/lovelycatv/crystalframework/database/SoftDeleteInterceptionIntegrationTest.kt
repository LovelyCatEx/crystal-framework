package com.lovelycatv.crystalframework.database

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.config.database.CrystalFrameworkSQLModifier
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Regression guard for M-11: three [com.lovelycatv.crystalframework.shared.types.entity.BaseEntity]
 * tables bypassed the soft-delete SQL interceptor because their registered names did not match the
 * physical table names ([TableConstants.TABLE_MESSAGE_CHANNELS] was registered as the dropped
 * `tenant_message_channels`, [TableConstants.TABLE_TENANT_MEMBER_PROFILES] as the never-existent
 * `tenant_user_profiles`) or were never registered ([TableConstants.TABLE_APPROVAL_FLOW_TOKEN]).
 *
 * Booting the full context runs [TableRegistryInitializer], which populates the singleton
 * [CrystalFrameworkSQLModifier]; asserting on its output proves both the registration wiring and
 * the SELECT / DELETE / UPDATE interception end-to-end at the SQL layer.
 */
class SoftDeleteInterceptionIntegrationTest : CrystalFrameworkApplicationTests() {

    private val fixedTables = listOf(
        TableConstants.TABLE_MESSAGE_CHANNELS,
        TableConstants.TABLE_TENANT_MEMBER_PROFILES,
        TableConstants.TABLE_APPROVAL_FLOW_TOKEN,
    )

    @Test
    fun selectAppendsSoftDeleteCondition() {
        fixedTables.forEach { table ->
            val result = CrystalFrameworkSQLModifier.processSql("SELECT * FROM $table WHERE id = 1")
            assertTrue(
                result.contains("deleted_time IS NULL", ignoreCase = true),
                "SELECT on '$table' must be filtered by deleted_time IS NULL, got: $result"
            )
        }
    }

    @Test
    fun deleteIsConvertedToSoftDeleteUpdate() {
        fixedTables.forEach { table ->
            val result = CrystalFrameworkSQLModifier.processSql("DELETE FROM $table WHERE id = 1")
            assertTrue(
                result.trimStart().startsWith("UPDATE", ignoreCase = true),
                "DELETE on '$table' must be rewritten as an UPDATE, got: $result"
            )
            assertTrue(
                result.contains("deleted_time", ignoreCase = true) &&
                    result.contains("modified_time", ignoreCase = true),
                "Soft-delete UPDATE on '$table' must set deleted_time and modified_time, got: $result"
            )
        }
    }

    @Test
    fun updateMaintainsModifiedTime() {
        fixedTables.forEach { table ->
            val result = CrystalFrameworkSQLModifier.processSql("UPDATE $table SET name = 'x' WHERE id = 1")
            assertTrue(
                result.contains("modified_time", ignoreCase = true),
                "UPDATE on '$table' must maintain modified_time, got: $result"
            )
        }
    }

    @Test
    fun unregisteredTableIsLeftUnchanged() {
        val sql = "SELECT * FROM a_table_that_is_not_registered WHERE id = 1"
        val result = CrystalFrameworkSQLModifier.processSql(sql)
        assertFalse(
            result.contains("deleted_time", ignoreCase = true),
            "Non-BaseEntity tables must not be touched by the soft-delete interceptor, got: $result"
        )
    }
}
