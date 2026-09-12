package com.lovelycatv.crystalframework.ai.types

data class AiGenericHttpConfig(
    val method: AiHttpMethod,
    val path: String,
    val headers: Map<String, String> = emptyMap(),
    val bodyTemplate: Map<String, Any?> = emptyMap(),
    val response: AiEndpointResponseConfig,
)
