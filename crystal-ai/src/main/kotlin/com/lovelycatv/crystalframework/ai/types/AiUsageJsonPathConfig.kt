package com.lovelycatv.crystalframework.ai.types

data class AiUsageJsonPathConfig(
    val inputTokensPath: String? = null,
    val outputTokensPath: String? = null,
    val totalTokensPath: String? = null,
    val cacheReadTokensPath: String? = null,
    val cacheWriteTokensPath: String? = null,
)
