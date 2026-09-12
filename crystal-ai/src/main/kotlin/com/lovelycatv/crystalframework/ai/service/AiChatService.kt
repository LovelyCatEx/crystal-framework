package com.lovelycatv.crystalframework.ai.service

import com.lovelycatv.vertex.ai.llm.ChatResponse
import com.lovelycatv.vertex.ai.llm.ReasoningEffort
import com.lovelycatv.vertex.ai.llm.StreamChatResponse
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
     * Perform synchronous chat completion
     *
     * @param modelId The ID of the AI model to use
     * @param messages The list of chat messages
     * @param reasoningEffort How hard the model should think. `null` leaves the choice to the
     *   provider's protocol.
     * @return ChatResponse containing the completion result
     */
    suspend fun chatCompletionSync(
        modelId: Long,
        messages: List<ChatMessage>,
        reasoningEffort: ReasoningEffort? = null
    ): ChatResponse

    /**
     * Perform asynchronous streaming chat completion
     *
     * The provider is contacted when the returned flow is collected, not when this function is
     * called.
     *
     * @param modelId The ID of the AI model to use
     * @param messages The list of chat messages
     * @param reasoningEffort How hard the model should think. `null` leaves the choice to the
     *   provider's protocol.
     * @return Flow of response chunks as they arrive
     */
    suspend fun chatCompletionAsync(
        modelId: Long,
        messages: List<ChatMessage>,
        reasoningEffort: ReasoningEffort? = null
    ): Flow<StreamChatResponse>
}
