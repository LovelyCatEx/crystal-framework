package com.lovelycatv.crystalframework.ai.controller.manager.playground.vo

/**
 * Token usage for a single completion, shown in the playground next to the assistant reply.
 *
 * `promptTokens` and `completionTokens` are always shown; the reasoning and cache fields are
 * shown only when non-zero, since most providers report them as zero when unused.
 */
data class ManagerAiPlaygroundUsageVO(
    val promptTokens: Int,
    val completionTokens: Int,
    val reasoningTokens: Int,
    val cachedPromptTokens: Int,
    val cacheCreationTokens: Int,
)
