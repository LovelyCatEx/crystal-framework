package com.lovelycatv.crystalframework.ai.service

import com.lovelycatv.crystalframework.ai.types.AiChatCompletionResult
import com.lovelycatv.crystalframework.ai.types.AiChatStreamEvent
import com.lovelycatv.vertex.ai.llm.ReasoningEffort
import com.lovelycatv.vertex.ai.llm.message.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * AI Chat Completion Service
 *
 * Provides synchronous and asynchronous chat completion capabilities
 * using the configured AI providers and models
 */
interface AiChatService {
    /**
     * Perform synchronous chat completion, running any tool calls the model asks for and returning
     * the final answer together with the tool-call steps executed along the way.
     *
     * @param modelId The ID of the AI model to use
     * @param messages The list of chat messages
     * @param reasoningEffort How hard the model should think. `null` leaves the choice to the
     *   provider's protocol.
     * @return The completion result plus every tool call that ran.
     */
    suspend fun chatCompletionSync(
        modelId: Long,
        messages: List<ChatMessage>,
        reasoningEffort: ReasoningEffort? = null
    ): AiChatCompletionResult

    /**
     * Perform asynchronous streaming chat completion.
     *
     * The provider is contacted when the returned flow is collected, not when this function is
     * called.
     *
     * @param modelId The ID of the AI model to use
     * @param messages The list of chat messages
     * @param reasoningEffort How hard the model should think. `null` leaves the choice to the
     *   provider's protocol.
     * @return Flow of text chunks interleaved with tool-call events as they happen.
     */
    suspend fun chatCompletionAsync(
        modelId: Long,
        messages: List<ChatMessage>,
        reasoningEffort: ReasoningEffort? = null
    ): Flow<AiChatStreamEvent>
}
