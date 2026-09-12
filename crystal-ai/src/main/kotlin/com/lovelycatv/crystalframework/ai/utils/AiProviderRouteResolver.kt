package com.lovelycatv.crystalframework.ai.utils

import com.lovelycatv.crystalframework.ai.constants.AiProviderProtocolDefaults
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.types.AiProviderProtocolType

/**
 * Resolves the `chatCompletionPath` handed to VertexLib's `LLMClientConfig`.
 *
 * VertexLib composes the URL as `baseUrl` + path, whereas the previous Spring AI setup let the
 * vendor SDK append the route itself. The two protocols therefore ended up stored with different
 * `baseUrl` conventions in `ai_providers`:
 *
 *  - OpenAI-compatible rows carry the version segment (`https://api.deepseek.com/v1`), because the
 *    OpenAI SDK appends only `/chat/completions`.
 *  - Anthropic-compatible rows are host-only (`https://api.anthropic.com`), because the Anthropic
 *    SDK appends the whole `/v1/messages`.
 *
 * So the Messages route is only sent bare when the stored `baseUrl` already ends in a version
 * segment, and the version is added otherwise. OpenAI-compatible rows keep the bare route.
 *
 * The stored `chat_completions_path` column is deliberately not used: the previous implementation
 * never read it, so its contents are unverified — and its frontend placeholder
 * (`/v1/chat/completions`) would duplicate a version segment that `baseUrl` already contains.
 * Honouring stored paths is a separate change that needs a data migration.
 */
fun AiProviderEntity.resolveChatCompletionPath(): String {
    return when (getRealProtocolType()) {
        AiProviderProtocolType.OPENAI_COMPATIBLE ->
            AiProviderProtocolDefaults.OPENAI_CHAT_COMPLETIONS_PATH

        AiProviderProtocolType.ANTHROPIC_MESSAGES ->
            if (AiProviderProtocolDefaults.TRAILING_API_VERSION_PATTERN.containsMatchIn(baseUrl)) {
                AiProviderProtocolDefaults.ANTHROPIC_MESSAGES_PATH
            } else {
                AiProviderProtocolDefaults.ANTHROPIC_VERSIONED_MESSAGES_PATH
            }
    }
}
