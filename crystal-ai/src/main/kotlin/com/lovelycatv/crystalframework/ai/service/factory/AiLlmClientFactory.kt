package com.lovelycatv.crystalframework.ai.service.factory

import com.lovelycatv.crystalframework.ai.constants.AiProviderProtocolDefaults
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.types.AiProviderProtocolType
import com.lovelycatv.crystalframework.ai.types.AiProviderRequestConfig
import com.lovelycatv.crystalframework.ai.utils.resolveChatCompletionPath
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.vertex.ai.llm.LLMClient
import com.lovelycatv.vertex.ai.llm.config.LLMClientConfig
import com.lovelycatv.vertex.ai.llm.config.LLMResponseConfig
import com.lovelycatv.vertex.ai.llm.impl.AnthropicLLMClient
import com.lovelycatv.vertex.ai.llm.impl.OpenAiLLMClient
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * Builds — and keeps — the VertexLib [LLMClient] for an AI provider.
 *
 * One client is cached per provider so that its OkHttp connection pool outlives a single request.
 * The entry is dropped whenever [AiProviderEntity.modifiedTime] moves on, which
 * `BaseManagerService.update` refreshes on every edit, so an operator changing a key or a base URL
 * gets a fresh client on the next call without any explicit invalidation wiring.
 *
 * Known limitation: VertexLib's `AnthropicLLMClient` keeps per-stream state — the response id and
 * the streaming tool-call buffers — on the instance itself, so two streams running concurrently
 * through the same provider would interleave. Handing out a single client per provider therefore
 * makes concurrent Anthropic streaming unsafe until that state moves into the returned Flow. The
 * synchronous path, which is what the playground uses, is unaffected.
 */
@Component
class AiLlmClientFactory {
    private val clients = ConcurrentHashMap<Long, CachedClient>()

    fun getClient(provider: AiProviderEntity): LLMClient {
        val cached = clients[provider.id]
        if (cached != null && cached.modifiedTime == provider.modifiedTime) {
            return cached.client
        }

        val entry = CachedClient(provider.modifiedTime, buildClient(provider))
        clients[provider.id] = entry
        return entry.client
    }

    private fun buildClient(provider: AiProviderEntity): LLMClient {
        val protocolType = provider.getRealProtocolType()
        val requestConfig = provider.getRequestConfigObject<AiProviderRequestConfig>()
        val responseConfig = provider.readResponseConfig()

        val config = LLMClientConfig(
            baseUrl = provider.baseUrl,
            apiKey = provider.apiKey,
            readTimeoutSeconds = AiProviderProtocolDefaults.READ_TIMEOUT_SECONDS,
            chatCompletionPath = provider.resolveChatCompletionPath(),
            llmResponseConfig = responseConfig,
            headers = resolveHeaders(protocolType, requestConfig),
        )

        return when (protocolType) {
            AiProviderProtocolType.OPENAI_COMPATIBLE -> OpenAiLLMClient(config)
            AiProviderProtocolType.ANTHROPIC_MESSAGES -> AnthropicLLMClient(config)
        }
    }

    /**
     * Reads `ai_providers.response_config`, which is stored shaped like VertexLib's
     * [LLMResponseConfig].
     *
     * Providers saved before that type was adopted hold the older key names (`inputTokensPath`
     * rather than `promptTokensPath`, plus a few fields with no counterpart). Those do not
     * deserialize at all, so rather than let the parser's `UnrecognizedPropertyException` surface
     * as an opaque 500, the failure names the provider and says what fixes it. Re-saving a provider
     * through the admin UI rewrites its config in the current shape.
     */
    private fun AiProviderEntity.readResponseConfig(): LLMResponseConfig {
        return try {
            getResponseConfigObject<LLMResponseConfig>()
        } catch (expected: Exception) {
            throw BusinessException(
                "AI provider '$name' stores its response config in the previous, superseded format " +
                    "and cannot be read. Re-save the provider to rewrite it.",
                expected,
            )
        }
    }

    /**
     * The Messages API rejects a request that does not name the API version, and VertexLib only
     * sends `x-api-key` — the vendor SDK it replaces added the version header on our behalf. A
     * provider header of the same name wins, so an operator can still pin another version.
     */
    private fun resolveHeaders(
        protocolType: AiProviderProtocolType,
        requestConfig: AiProviderRequestConfig,
    ): Map<String, String> {
        return when (protocolType) {
            AiProviderProtocolType.OPENAI_COMPATIBLE -> requestConfig.headers

            AiProviderProtocolType.ANTHROPIC_MESSAGES -> mapOf(
                AiProviderProtocolDefaults.ANTHROPIC_API_VERSION_HEADER to
                    AiProviderProtocolDefaults.ANTHROPIC_API_VERSION
            ) + requestConfig.headers
        }
    }

    private data class CachedClient(
        val modifiedTime: Long,
        val client: LLMClient,
    )
}
