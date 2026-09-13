/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.controller.manager.playground.dto

import com.lovelycatv.vertex.ai.llm.ReasoningEffort
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class ManagerAiPlaygroundChatDTO(
    @field:NotBlank(message = "Model ID is required")
    val modelId: String,
    @field:NotEmpty(message = "Messages are required")
    @field:Size(max = 100, message = "Messages cannot exceed 100 items")
    @field:Valid
    val messages: List<ManagerAiPlaygroundMessageDTO>,
    /**
     * Left unset to keep whatever the provider's protocol picks by default. The enum carries no
     * `typeId`, so it travels by name the way `ForbiddenReason` does.
     */
    val reasoningEffort: ReasoningEffort? = null,
    val sessionId: String? = null,
    @field:NotBlank(message = "Group ID is required")
    val groupId: String,
)
