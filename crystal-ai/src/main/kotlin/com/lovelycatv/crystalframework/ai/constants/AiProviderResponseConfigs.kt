package com.lovelycatv.crystalframework.ai.constants

import com.lovelycatv.crystalframework.ai.types.AiEndpointResponseConfig
import com.lovelycatv.crystalframework.ai.types.AiProviderResponseConfig
import com.lovelycatv.crystalframework.ai.types.AiUsageJsonPathConfig

object AiProviderResponseConfigs {
    val OPENAI = AiProviderResponseConfig(
        chatCompletions = AiEndpointResponseConfig(
            contentPath = "$.choices[0].message.content",
            finishReasonPath = "$.choices[0].finish_reason",
            providerRequestIdPath = "$.id",
            usage = AiUsageJsonPathConfig(
                inputTokensPath = "$.usage.prompt_tokens",
                outputTokensPath = "$.usage.completion_tokens",
                totalTokensPath = "$.usage.total_tokens",
                cacheReadTokensPath = "$.usage.prompt_tokens_details.cached_tokens",
            ),
            errorMessagePath = "$.error.message",
        ),
        embedding = AiEndpointResponseConfig(
            contentPath = "$.data[0].embedding",
            providerRequestIdPath = "$.id",
            usage = AiUsageJsonPathConfig(
                inputTokensPath = "$.usage.prompt_tokens",
                totalTokensPath = "$.usage.total_tokens",
            ),
            errorMessagePath = "$.error.message",
        ),
    )

    val ANTHROPIC = AiProviderResponseConfig(
        chatCompletions = AiEndpointResponseConfig(
            contentPath = "$.content[0].text",
            finishReasonPath = "$.stop_reason",
            providerRequestIdPath = "$.id",
            usage = AiUsageJsonPathConfig(
                inputTokensPath = "$.usage.input_tokens",
                outputTokensPath = "$.usage.output_tokens",
                cacheReadTokensPath = "$.usage.cache_read_input_tokens",
                cacheWriteTokensPath = "$.usage.cache_creation_input_tokens",
            ),
            errorMessagePath = "$.error.message",
        ),
    )
}
