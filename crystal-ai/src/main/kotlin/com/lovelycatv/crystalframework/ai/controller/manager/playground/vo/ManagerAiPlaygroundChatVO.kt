/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.controller.manager.playground.vo

import com.lovelycatv.crystalframework.ai.types.AiToolCallResult

data class ManagerAiPlaygroundChatVO(
    val content: String,
    val reasoningContent: String? = null,
    val usage: ManagerAiPlaygroundUsageVO? = null,
    val toolCalls: List<AiToolCallResult>? = null,
)