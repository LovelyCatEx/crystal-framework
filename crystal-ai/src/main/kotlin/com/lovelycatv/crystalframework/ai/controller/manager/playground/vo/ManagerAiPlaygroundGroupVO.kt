/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.controller.manager.playground.vo

import java.math.BigDecimal

/**
 * A user group as shown in the playground: its name, billing multiplier and the enabled models the
 * caller may pick from within it.
 */
data class ManagerAiPlaygroundGroupVO(
    val name: String,
    val billingMultiplier: BigDecimal,
    val modelIds: List<String>,
)
