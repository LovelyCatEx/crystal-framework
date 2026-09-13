/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.controller.manager.dto

import jakarta.validation.constraints.NotNull

class StartApprovalFlowDTO(
    @field:NotNull
    val definitionId: Long? = null,
    val formData: String = "{}",
)
