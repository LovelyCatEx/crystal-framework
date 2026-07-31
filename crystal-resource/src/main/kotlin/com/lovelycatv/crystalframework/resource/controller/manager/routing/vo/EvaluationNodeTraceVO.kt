package com.lovelycatv.crystalframework.resource.controller.manager.routing.vo

/**
 * Flat wire-format for [com.lovelycatv.crystalframework.shared.database.EvaluationNodeTrace]. A
 * `type` discriminator ("leaf" or "group") tells the frontend which fields to read; leaf fields
 * (field / operator / expectedValue / actualValue) and group fields (logic / children) are
 * mutually exclusive per node. Kept flat instead of a sealed hierarchy so it serializes cleanly
 * without JsonTypeInfo config.
 */
data class EvaluationNodeTraceVO(
    val type: String,
    val field: String? = null,
    val operator: String? = null,
    val expectedValue: String? = null,
    val actualValue: String? = null,
    val logic: String? = null,
    val children: List<EvaluationNodeTraceVO>? = null,
    val matched: Boolean,
)
