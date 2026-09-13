package com.lovelycatv.crystalframework.ai.controller.manager.playground.vo

import com.lovelycatv.crystalframework.ai.types.AiToolCallResult

data class ManagerAiPlaygroundChatVO(
    val content: String,
    val reasoningContent: String? = null,
    val usage: ManagerAiPlaygroundUsageVO? = null,
    val toolCalls: List<AiToolCallResult>? = null,
)