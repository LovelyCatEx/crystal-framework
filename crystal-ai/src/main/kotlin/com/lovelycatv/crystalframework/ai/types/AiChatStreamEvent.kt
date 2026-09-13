package com.lovelycatv.crystalframework.ai.types

import com.lovelycatv.vertex.ai.llm.StreamChatResponse

/**
 * One element of a streamed chat completion.
 *
 * [Chunk] carries a text/thinking delta from the model; [ToolCall] carries the outcome of a tool
 * the model asked to run, interleaved between the chunks of the rounds that produce it.
 */
sealed class AiChatStreamEvent {
    data class Chunk(val chunk: StreamChatResponse) : AiChatStreamEvent()

    data class ToolCall(val result: AiToolCallResult) : AiChatStreamEvent()
}
