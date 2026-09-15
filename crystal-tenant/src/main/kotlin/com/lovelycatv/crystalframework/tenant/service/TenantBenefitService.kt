/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.service

interface TenantBenefitService {
    suspend fun getBenefitValue(tireTypeId: Long, featureKey: String): String?

    suspend fun hasBenefit(tireTypeId: Long, featureKey: String): Boolean

    suspend fun getBenefitLimit(tireTypeId: Long, featureKey: String, defaultLimit: Long = 0): Long

    suspend fun getAllBenefitsForTireType(tireTypeId: Long): Map<String, String>
}
