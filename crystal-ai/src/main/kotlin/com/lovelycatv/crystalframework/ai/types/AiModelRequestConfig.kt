package com.lovelycatv.crystalframework.ai.types

import java.math.BigDecimal
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class AiModelRequestConfig(
    val temperature: BigDecimal? = null,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val maxOutputTokens: Long? = null,
    val additionalBody: Map<String, Any?> = emptyMap(),
)
