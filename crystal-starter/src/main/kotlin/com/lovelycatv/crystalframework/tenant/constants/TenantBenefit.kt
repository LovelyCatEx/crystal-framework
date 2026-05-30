package com.lovelycatv.crystalframework.tenant.constants

import com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.types.TenantBenefitDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.types.TenantBenefitType
import com.lovelycatv.crystalframework.shared.utils.KotlinObjectClassUtils

object TenantBenefit {
    val INVITATION_MAX_COUNT = TenantBenefitDeclaration(
        featureKey = "invitation.max_count",
        name = "Invitation Code Create Limit",
        description = "Maximum number of invitation codes a tenant can create in total",
        featureType = TenantBenefitType.LIMIT,
        defaultValue = "10",
    )

    val INVITATION_PER_DAY_COUNT = TenantBenefitDeclaration(
        featureKey = "invitation.per_day_count",
        name = "Daily Invitation Create Limit",
        description = "Number of invitation codes a tenant can create per day",
        featureType = TenantBenefitType.LIMIT,
        defaultValue = "5",
    )

    val INVITATION_PER_CODE_USAGE_LIMIT = TenantBenefitDeclaration(
        featureKey = "invitation.per_code_usage_limit",
        name = "Per-Code Usage Limit",
        description = "Maximum number of times a single invitation code can be used",
        featureType = TenantBenefitType.LIMIT,
        defaultValue = "1",
    )

    val INVITATION_MAX_VALIDITY_DAYS = TenantBenefitDeclaration(
        featureKey = "invitation.max_validity_days",
        name = "Invitation Max Validity",
        description = "Maximum validity period in days for an invitation code",
        featureType = TenantBenefitType.LIMIT,
        defaultValue = "30",
    )

    val MEMBER_MAX_COUNT = TenantBenefitDeclaration(
        featureKey = "member.max_count",
        name = "Member Limit",
        description = "Maximum number of members a tenant can have",
        featureType = TenantBenefitType.LIMIT,
        defaultValue = "20",
    )

    val DEPARTMENT_MAX_COUNT = TenantBenefitDeclaration(
        featureKey = "department.max_count",
        name = "Department Limit",
        description = "Maximum number of departments a tenant can create",
        featureType = TenantBenefitType.LIMIT,
        defaultValue = "5",
    )

    val EXPORT_DATA_ENABLED = TenantBenefitDeclaration(
        featureKey = "feature.export_data",
        name = "Data Export",
        description = "Enable data export functionality",
        featureType = TenantBenefitType.BOOLEAN,
        defaultValue = "false",
    )

    val ADVANCED_ANALYTICS_ENABLED = TenantBenefitDeclaration(
        featureKey = "feature.advanced_analytics",
        name = "Advanced Analytics",
        description = "Enable advanced data analytics functionality",
        featureType = TenantBenefitType.BOOLEAN,
        defaultValue = "false",
    )

    val API_ACCESS_ENABLED = TenantBenefitDeclaration(
        featureKey = "feature.api_access",
        name = "API Access",
        description = "Enable API access functionality",
        featureType = TenantBenefitType.BOOLEAN,
        defaultValue = "false",
    )

    fun allBenefits(): List<TenantBenefitDeclaration> {
        return KotlinObjectClassUtils.extractAllValProperties(TenantBenefit, false)
    }
}
