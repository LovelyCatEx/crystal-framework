/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.resource.controller.manager.routing.dto

import jakarta.validation.constraints.NotEmpty

data class ManagerReorderStorageProviderRoutingRuleDTO(
    @field:NotEmpty(message = "Ordered ids is required")
    val orderedIds: List<Long>
)
