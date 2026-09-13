/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.vo

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class ManagerReadTenantTireBenefitOverviewItemVO(
    val featureId: Long,
    val featureKey: String,
    val name: String,
    val description: String?,
    val featureType: Int,
    val defaultValue: String?,
    val value: String?,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val valueId: Long?,
    val isCustomized: Boolean,
)
