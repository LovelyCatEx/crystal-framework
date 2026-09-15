/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.constants

/**
 * Per-protocol defaults handed to VertexLib's `LLMClientConfig`.
 */
object AiProviderProtocolDefaults {
    const val OPENAI_CHAT_COMPLETIONS_PATH = "chat/completions"

    /** Messages route for servers whose base URL already carries the version segment. */
    const val ANTHROPIC_MESSAGES_PATH = "messages"

    /** Messages route used when the version segment is ours to add. */
    const val ANTHROPIC_VERSIONED_MESSAGES_PATH = "v1/messages"

    /** Matches a trailing API version segment, e.g. the `/v1` of `https://api.deepseek.com/v1`. */
    val TRAILING_API_VERSION_PATTERN = Regex("/v\\d+[A-Za-z0-9]*/?$")

    const val ANTHROPIC_API_VERSION_HEADER = "anthropic-version"

    const val ANTHROPIC_API_VERSION = "2023-06-01"

    /**
     * VertexLib defaults this to 60s. Its previous Spring AI setup inherited a timeout that the
     * vendor SDKs scaled with `max_tokens`, so a long completion would now be cut off mid-stream.
     */
    const val READ_TIMEOUT_SECONDS = 600L
}
