package com.lovelycatv.crystalframework.ai.controller.manager.playground.vo

import java.math.BigDecimal

/**
 * A model as shown in the playground: display name, key, the four prices, capabilities, context
 * window and currency. Anything else (internal id, description, max output tokens, request config)
 * is deliberately not exposed.
 */
data class ManagerAiPlaygroundModelVO(
    val displayName: String,
    val key: String,
    val inputPricePerMillion: BigDecimal,
    val outputPricePerMillion: BigDecimal,
    val cacheReadPricePerMillion: BigDecimal?,
    val cacheWritePricePerMillion: BigDecimal?,
    val capabilities: Set<Int>,
    val contextWindowTokens: Long,
    val currency: String,
)
