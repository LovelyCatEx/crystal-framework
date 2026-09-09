package com.lovelycatv.crystalframework.ai.types

data class AiProviderResponseConfig(
    val chatCompletions: AiEndpointResponseConfig? = null,
    val embedding: AiEndpointResponseConfig? = null,
)
