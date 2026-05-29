package com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.types

data class TenantBenefitDeclaration(
    val featureKey: String,
    val name: String,
    val description: String = "",
    val featureType: TenantBenefitType,
    val defaultValue: String = "",
)
