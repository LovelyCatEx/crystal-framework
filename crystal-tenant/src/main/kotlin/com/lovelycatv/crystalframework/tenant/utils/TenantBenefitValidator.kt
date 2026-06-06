package com.lovelycatv.crystalframework.tenant.utils

import com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.types.TenantBenefitType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.tenant.entity.TenantTireBenefitFeatureEntity

object TenantBenefitValidator {

    /**
     * Validate a value against a [TenantBenefitType] by its [typeId].
     * Used for both feature defaultValue and benefit featureValue.
     *
     * @param featureTypeId the typeId from [TenantBenefitType]
     * @param value the value to validate
     * @param fieldName label used in error messages (e.g. "defaultValue", "featureValue")
     */
    fun validateByType(featureTypeId: Int, value: String, fieldName: String) {
        val featureType = TenantBenefitType.entries.find { it.typeId == featureTypeId }
            ?: throw BusinessException("Unknown feature type: $featureTypeId")

        when (featureType) {
            TenantBenefitType.BOOLEAN -> {
                if (value != "true" && value != "false") {
                    throw BusinessException("$fieldName must be 'true' or 'false' for BOOLEAN type, got: $value")
                }
            }
            TenantBenefitType.LIMIT -> {
                val intVal = value.toIntOrNull()
                    ?: throw BusinessException("$fieldName must be a valid integer for LIMIT type, got: $value")
                if (intVal < 0) {
                    throw BusinessException("$fieldName must be non-negative for LIMIT type, got: $intVal")
                }
            }
            TenantBenefitType.ENUM -> {
                if (value.isBlank()) {
                    throw BusinessException("$fieldName must not be blank for ENUM type")
                }
            }
        }
    }

    /**
     * For ENUM features: validate that [featureValue] belongs to the allowed options
     * defined in [feature.defaultValue]. Call this AFTER [validateByType].
     */
    fun validateEnumAllowedValue(feature: TenantTireBenefitFeatureEntity, featureValue: String) {
        val allowedOptions = feature.defaultValue
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
        if (allowedOptions.isEmpty()) {
            throw BusinessException("ENUM feature '${feature.featureKey}' has no options defined in defaultValue")
        }
        if (featureValue !in allowedOptions) {
            throw BusinessException(
                "featureValue '$featureValue' must be one of [${allowedOptions.joinToString(", ")}] for ENUM feature '${feature.featureKey}'"
            )
        }
    }
}
