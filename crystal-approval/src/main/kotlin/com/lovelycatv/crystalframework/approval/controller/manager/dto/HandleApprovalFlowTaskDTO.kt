/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.controller.manager.dto

import jakarta.validation.constraints.NotNull

data class HandleApprovalFlowTaskDTO(
    @field:NotNull
    val taskId: Long? = null,
    @field:NotNull
    val approved: Boolean? = null,
    val comment: String? = null,
    val formData: String? = null,
)
