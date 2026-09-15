/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto

data class ManagerCreateTenantTireBenefitFeatureDTO(
    val featureKey: String = "",
    val name: String = "",
    val description: String? = null,
    val featureType: Int = 0,
    val defaultValue: String? = null,
)
