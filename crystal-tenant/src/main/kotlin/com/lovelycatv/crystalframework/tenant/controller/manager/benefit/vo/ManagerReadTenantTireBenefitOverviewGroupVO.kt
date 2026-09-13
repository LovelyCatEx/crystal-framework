/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.vo

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class ManagerReadTenantTireBenefitOverviewGroupVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    val tireTypeId: Long,
    val items: List<ManagerReadTenantTireBenefitOverviewItemVO>,
)
