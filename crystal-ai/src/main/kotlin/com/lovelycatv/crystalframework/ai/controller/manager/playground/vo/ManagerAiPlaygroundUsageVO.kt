/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
