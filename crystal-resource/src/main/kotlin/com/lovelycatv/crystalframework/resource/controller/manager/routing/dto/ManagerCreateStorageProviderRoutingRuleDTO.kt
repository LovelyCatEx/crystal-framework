/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.resource.controller.manager.routing.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ManagerCreateStorageProviderRoutingRuleDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String,

    val conditionTree: String? = null,

    @field:NotBlank(message = "Target provider ids is required")
    val targetProviderIds: String,

    @field:NotNull(message = "Distribution type is required")
    val distributionType: Int,

    val enabled: Boolean = true,
)
