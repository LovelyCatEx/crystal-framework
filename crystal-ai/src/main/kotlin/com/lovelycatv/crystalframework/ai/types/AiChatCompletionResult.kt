/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.types

import com.lovelycatv.vertex.ai.llm.ChatResponse

/**
 * The result of a synchronous chat completion: the final [response] (its `usage` is already summed
 * across tool-calling rounds) plus every [toolCalls] step executed along the way.
 */
data class AiChatCompletionResult(
    val response: ChatResponse,
    val toolCalls: List<AiToolCallResult>,
)
