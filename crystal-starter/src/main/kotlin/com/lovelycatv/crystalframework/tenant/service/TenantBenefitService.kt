package com.lovelycatv.crystalframework.tenant.service

interface TenantBenefitService {
    suspend fun getBenefitValue(tireTypeId: Long, featureKey: String): String?

    suspend fun hasBenefit(tireTypeId: Long, featureKey: String): Boolean

    suspend fun getBenefitLimit(tireTypeId: Long, featureKey: String, defaultLimit: Long = 0): Long

    suspend fun getAllBenefitsForTireType(tireTypeId: Long): Map<String, String>
}
