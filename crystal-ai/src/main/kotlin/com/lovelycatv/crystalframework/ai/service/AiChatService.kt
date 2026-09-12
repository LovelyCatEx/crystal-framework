package com.lovelycatv.crystalframework.ai.service

import kotlinx.coroutines.flow.Flow
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.model.ChatResponse

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
     * @return ChatResponse containing the completion result
     */
    suspend fun chatCompletionSync(
        modelId: Long,
        messages: List<Message>
    ): ChatResponse

    /**
     * Perform asynchronous streaming chat completion
     *
     * @param modelId The ID of the AI model to use
     * @param messages The list of chat messages
     * @return Flow of response chunks as they arrive
     */
    suspend fun chatCompletionAsync(
        modelId: Long,
        messages: List<Message>
    ): Flow<ChatResponse>
}
