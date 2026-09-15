/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto

data class ManagerCreateTenantTireBenefitValueDTO(
    val tireTypeId: Long = 0,
    val featureId: Long = 0,
    val featureValue: String = "",
)
