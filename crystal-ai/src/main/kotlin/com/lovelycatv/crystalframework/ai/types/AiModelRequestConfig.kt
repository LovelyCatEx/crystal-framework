/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.types

import java.math.BigDecimal
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class AiModelRequestConfig(
    val temperature: BigDecimal? = null,
    val topP: BigDecimal? = null,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val maxOutputTokens: Long? = null,
    val additionalBody: Map<String, Any?> = emptyMap(),
)
