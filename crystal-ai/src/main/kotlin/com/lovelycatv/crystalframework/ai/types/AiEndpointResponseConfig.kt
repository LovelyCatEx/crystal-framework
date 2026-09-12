package com.lovelycatv.crystalframework.ai.types

data class AiEndpointResponseConfig(
    val contentPath: String? = null,
    val finishReasonPath: String? = null,
    val providerRequestIdPath: String? = null,
    val usage: AiUsageJsonPathConfig? = null,
    val errorMessagePath: String? = null,
)
