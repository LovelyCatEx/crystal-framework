/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.controller.manager.playground.vo

import com.lovelycatv.crystalframework.ai.types.AiToolCallResult

/**
 * One frame of a streamed chat completion, sent as a single SSE `data` payload.
 *
 * [content] and [reasoningContent] are the *increment* contributed by this frame, not the running
 * total — the client concatenates them. A frame that carries no text (e.g. Anthropic's
 * `message_stop`) has both null. [finished] is true on the frame that closes the stream. When the
 * model asks for a tool, a dedicated frame carries [toolCall] instead of text.
 */
data class ManagerAiPlaygroundStreamChunkVO(
    val content: String? = null,
    val reasoningContent: String? = null,
    val finished: Boolean,
    val usage: ManagerAiPlaygroundUsageVO? = null,
    val toolCall: AiToolCallResult? = null,
)
