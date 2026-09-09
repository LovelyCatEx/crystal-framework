package com.lovelycatv.crystalframework.ai.controller.manager.model.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class ManagerUpdateAiModelDTO(
    override val id: Long,
    val providerId: Long? = null,
    @field:Size(max = 128, message = "Key length cannot exceed 128 characters")
    val key: String? = null,
    @field:Size(max = 256, message = "Model name length cannot exceed 256 characters")
    val modelName: String? = null,
    @field:Size(max = 128, message = "Display name length cannot exceed 128 characters")
    val displayName: String? = null,
    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,
    val capabilities: String? = null,
    @field:DecimalMin(value = "1", inclusive = true)
    val contextWindowTokens: Long? = null,
    @field:PositiveOrZero(message = "Max output tokens must not be negative")
    val maxOutputTokens: Long? = null,
    @field:DecimalMin(value = "0.0", inclusive = true)
    val inputPricePerMillion: BigDecimal? = null,
    @field:DecimalMin(value = "0.0", inclusive = true)
    val outputPricePerMillion: BigDecimal? = null,
    @field:DecimalMin(value = "0.0", inclusive = true)
    val cacheReadPricePerMillion: BigDecimal? = null,
    @field:DecimalMin(value = "0.0", inclusive = true)
    val cacheWritePricePerMillion: BigDecimal? = null,
    @field:Size(max = 16, message = "Currency length cannot exceed 16 characters")
    val currency: String? = null,
    val requestConfig: String? = null,
    val enabled: Boolean? = null,
    val sort: Int? = null,
) : BaseManagerUpdateDTO(id)
