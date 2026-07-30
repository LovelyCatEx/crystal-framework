package com.lovelycatv.crystalframework.shared.database

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class QueryNodeEvaluatorTest {

    private fun eval(node: QueryNode, context: Map<String, Any?>): Boolean =
        QueryNodeEvaluator.evaluate(node, context)

    @Test
    fun `EQ matches numerically equal values across Int, Long and String representations`() {
        val node = ConditionNode("fileType", QueryOperator.EQ, 0)
        assertTrue(eval(node, mapOf("fileType" to 0)))
        assertTrue(eval(node, mapOf("fileType" to 0L)))
        assertTrue(eval(node, mapOf("fileType" to "0")))
        assertFalse(eval(node, mapOf("fileType" to 1)))
    }

    @Test
    fun `NE is the negation of EQ`() {
        val node = ConditionNode("fileType", QueryOperator.NE, 0)
        assertFalse(eval(node, mapOf("fileType" to 0)))
        assertTrue(eval(node, mapOf("fileType" to 1)))
    }

    @Test
    fun `CONTAINS checks substring on string representation`() {
        val node = ConditionNode("fileName", QueryOperator.CONTAINS, "avatar")
        assertTrue(eval(node, mapOf("fileName" to "user-avatar-123.png")))
        assertFalse(eval(node, mapOf("fileName" to "banner.png")))
    }

    @Test
    fun `LIKE supports percent and underscore wildcards`() {
        val node = ConditionNode("fileExtension", QueryOperator.LIKE, "j_g")
        assertTrue(eval(node, mapOf("fileExtension" to "jpg")))
        assertFalse(eval(node, mapOf("fileExtension" to "jpeg")))

        val prefixNode = ConditionNode("fileContentType", QueryOperator.LIKE, "image/%")
        assertTrue(eval(prefixNode, mapOf("fileContentType" to "image/png")))
        assertFalse(eval(prefixNode, mapOf("fileContentType" to "video/mp4")))
    }

    @Test
    fun `numeric comparisons work across Int Long and String`() {
        assertTrue(eval(ConditionNode("fileSize", QueryOperator.GT, 1000L), mapOf("fileSize" to 2000L)))
        assertFalse(eval(ConditionNode("fileSize", QueryOperator.GT, 1000L), mapOf("fileSize" to 500L)))
        assertTrue(eval(ConditionNode("fileSize", QueryOperator.GTE, "1000"), mapOf("fileSize" to 1000L)))
        assertTrue(eval(ConditionNode("hourOfDay", QueryOperator.LTE, 23), mapOf("hourOfDay" to 23)))
        assertTrue(eval(ConditionNode("hourOfDay", QueryOperator.LT, 23), mapOf("hourOfDay" to 8)))
    }

    @Test
    fun `IN matches any value in the list regardless of numeric type`() {
        val node = ConditionNode("fileType", QueryOperator.IN, values = listOf(0, 1, 2))
        assertTrue(eval(node, mapOf("fileType" to 1)))
        assertFalse(eval(node, mapOf("fileType" to 5)))
    }

    @Test
    fun `IS_NULL and IS_NOT_NULL check map presence and null value`() {
        assertTrue(eval(ConditionNode("tenantId", QueryOperator.IS_NULL), mapOf("tenantId" to null)))
        assertTrue(eval(ConditionNode("tenantId", QueryOperator.IS_NULL), emptyMap()))
        assertTrue(eval(ConditionNode("tenantId", QueryOperator.IS_NOT_NULL), mapOf("tenantId" to 42L)))
        assertFalse(eval(ConditionNode("tenantId", QueryOperator.IS_NOT_NULL), mapOf("tenantId" to null)))
    }

    @Test
    fun `AND group requires all children to match`() {
        val group = GroupNode(
            QueryLogic.AND,
            listOf(
                ConditionNode("fileType", QueryOperator.EQ, 0),
                ConditionNode("fileSize", QueryOperator.LT, 1000L),
            )
        )
        assertTrue(eval(group, mapOf("fileType" to 0, "fileSize" to 500L)))
        assertFalse(eval(group, mapOf("fileType" to 0, "fileSize" to 5000L)))
    }

    @Test
    fun `OR group requires at least one child to match`() {
        val group = GroupNode(
            QueryLogic.OR,
            listOf(
                ConditionNode("fileType", QueryOperator.EQ, 0),
                ConditionNode("fileType", QueryOperator.EQ, 1),
            )
        )
        assertTrue(eval(group, mapOf("fileType" to 1)))
        assertFalse(eval(group, mapOf("fileType" to 2)))
    }

    @Test
    fun `nested groups evaluate recursively`() {
        val nested = GroupNode(
            QueryLogic.AND,
            listOf(
                ConditionNode("fileType", QueryOperator.EQ, 0),
                GroupNode(
                    QueryLogic.OR,
                    listOf(
                        ConditionNode("hourOfDay", QueryOperator.LT, 6),
                        ConditionNode("hourOfDay", QueryOperator.GTE, 22),
                    )
                )
            )
        )
        assertTrue(eval(nested, mapOf("fileType" to 0, "hourOfDay" to 23)))
        assertFalse(eval(nested, mapOf("fileType" to 0, "hourOfDay" to 12)))
        assertFalse(eval(nested, mapOf("fileType" to 1, "hourOfDay" to 23)))
    }
}
