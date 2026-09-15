/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.types

data class TenantBenefitDeclaration(
    val featureKey: String,
    val name: String,
    val description: String = "",
    val featureType: TenantBenefitType,
    val defaultValue: String = "",
)
