package com.lovelycatv.crystalframework.ai.controller.manager.playground.vo

/**
 * One frame of a streamed chat completion, sent as a single SSE `data` payload.
 *
 * [content] and [reasoningContent] are the *increment* contributed by this frame, not the running
 * total — the client concatenates them. A frame that carries no text (e.g. Anthropic's
 * `message_stop`) has both null. [finished] is true on the frame that closes the stream.
 */
data class ManagerAiPlaygroundStreamChunkVO(
    val content: String? = null,
    val reasoningContent: String? = null,
    val finished: Boolean,
    val usage: ManagerAiPlaygroundUsageVO? = null,
)
