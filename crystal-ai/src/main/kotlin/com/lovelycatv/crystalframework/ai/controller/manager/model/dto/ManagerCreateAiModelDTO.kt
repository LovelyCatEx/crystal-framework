package com.lovelycatv.crystalframework.ai.controller.manager.model.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class ManagerCreateAiModelDTO(
    @field:NotNull(message = "Provider ID is required")
    val providerId: Long,
    @field:NotBlank(message = "Key is required")
    @field:Size(max = 128, message = "Key length cannot exceed 128 characters")
    val key: String,
    @field:NotBlank(message = "Model name is required")
    @field:Size(max = 256, message = "Model name length cannot exceed 256 characters")
    val modelName: String,
    @field:NotBlank(message = "Display name is required")
    @field:Size(max = 128, message = "Display name length cannot exceed 128 characters")
    val displayName: String,
    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,
    @field:NotBlank(message = "Capabilities are required")
    val capabilities: String = "[]",
    @field:Positive(message = "Context window tokens must be positive")
    val contextWindowTokens: Long,
    val maxOutputTokens: Long? = null,
    @field:DecimalMin(value = "0.0", inclusive = true)
    val inputPricePerMillion: BigDecimal = BigDecimal.ZERO,
    @field:DecimalMin(value = "0.0", inclusive = true)
    val outputPricePerMillion: BigDecimal = BigDecimal.ZERO,
    @field:DecimalMin(value = "0.0", inclusive = true)
    val cacheReadPricePerMillion: BigDecimal? = null,
    @field:DecimalMin(value = "0.0", inclusive = true)
    val cacheWritePricePerMillion: BigDecimal? = null,
    @field:NotBlank(message = "Currency is required")
    @field:Size(max = 16, message = "Currency length cannot exceed 16 characters")
    val currency: String,
    @field:NotBlank(message = "Request config is required")
    val requestConfig: String = "{}",
    val enabled: Boolean = true,
    val sort: Int = 0,
)
