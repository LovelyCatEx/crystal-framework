package com.lovelycatv.crystalframework.shared.database

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.data.relational.core.query.CriteriaDefinition

class CrystalDatabaseExtensionsTest {

    private fun leaf(node: ConditionNode): CriteriaDefinition = criteriaFromQueryNode(node)

    @Test
    fun `EQ on stringified snowflake id is coerced to Long so binding matches bigint column`() {
        val criteria = leaf(ConditionNode("user_id", QueryOperator.EQ, "7467148669058285568"))
        val value = criteria.value
        assertTrue(value is Long, "expected Long, got ${value?.javaClass}")
        assertEquals(7467148669058285568L, value)
    }

    @Test
    fun `EQ on plain non-numeric string stays as String`() {
        val criteria = leaf(ConditionNode("username", QueryOperator.EQ, "alice"))
        assertEquals("alice", criteria.value)
    }

    @Test
    fun `EQ on mixed-digit string stays as String`() {
        val criteria = leaf(ConditionNode("code", QueryOperator.EQ, "abc123"))
        assertEquals("abc123", criteria.value)
    }

    @Test
    fun `EQ on negative numeric string is coerced to negative Long`() {
        val criteria = leaf(ConditionNode("delta", QueryOperator.EQ, "-42"))
        assertEquals(-42L, criteria.value)
    }

    @Test
    fun `EQ on native Long value passes through unchanged`() {
        val criteria = leaf(ConditionNode("user_id", QueryOperator.EQ, 42L))
        assertEquals(42L, criteria.value)
    }

    @Test
    fun `CONTAINS on numeric-looking string is NOT coerced`() {
        val criteria = leaf(ConditionNode("phone", QueryOperator.CONTAINS, "13800138000"))
        assertEquals("%13800138000%", criteria.value)
    }

    @Test
    fun `LIKE on numeric-looking string is NOT coerced`() {
        val criteria = leaf(ConditionNode("phone", QueryOperator.LIKE, "138%"))
        assertEquals("138%", criteria.value)
    }

    @Test
    fun `GTE on stringified timestamp is coerced to Long`() {
        val criteria = leaf(ConditionNode("created_time", QueryOperator.GTE, "1735689600000"))
        assertEquals(1735689600000L, criteria.value)
    }

    @Test
    fun `IN on stringified numeric list coerces every element`() {
        val criteria = leaf(
            ConditionNode(
                field = "user_id",
                operator = QueryOperator.IN,
                values = listOf("7467148669058285568", "42", "hybrid_string"),
            )
        )
        @Suppress("UNCHECKED_CAST")
        val values = criteria.value as List<Any>
        assertEquals(7467148669058285568L, values[0])
        assertEquals(42L, values[1])
        assertEquals("hybrid_string", values[2])
    }
}
