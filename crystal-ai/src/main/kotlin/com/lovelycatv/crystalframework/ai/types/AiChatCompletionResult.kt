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
