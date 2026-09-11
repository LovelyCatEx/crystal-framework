package com.lovelycatv.crystalframework.ai.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.ai.types.AiModelCapability
import com.lovelycatv.crystalframework.shared.annotations.NotQueryable
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.utils.parseObject
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer
import java.math.BigDecimal

@Table("ai_models")
class AiModelEntity(
    id: Long = 0,
    @Column("provider_id")
    var providerId: Long = 0,
    @Column("key")
    var key: String = "",
    @Column("model_name")
    var modelName: String = "",
    @Column("display_name")
    var displayName: String = "",
    @Column("description")
    var description: String? = null,
    @Column("capabilities")
    @field:NotQueryable
    var capabilities: String = "[]",
    @Column("context_window_tokens")
    var contextWindowTokens: Long = 0,
    @Column("max_output_tokens")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var maxOutputTokens: Long? = null,
    @Column("input_price_per_million")
    var inputPricePerMillion: BigDecimal = BigDecimal.ZERO,
    @Column("output_price_per_million")
    var outputPricePerMillion: BigDecimal = BigDecimal.ZERO,
    @Column("cache_read_price_per_million")
    var cacheReadPricePerMillion: BigDecimal? = null,
    @Column("cache_write_price_per_million")
    var cacheWritePricePerMillion: BigDecimal? = null,
    @Column("currency")
    var currency: String = "",
    @Column("request_config")
    @field:NotQueryable
    var requestConfig: String = "{}",
    @Column("enabled")
    var enabled: Boolean = true,
    @Column("sort")
    var sort: Int = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getRealCapabilities(): Set<AiModelCapability> {
        val ids: List<Int> = capabilities.parseObject()
        val values = ids.map { id ->
            AiModelCapability.getByTypeId(id)
                ?: throw BusinessException("ai model capability $id not found")
        }
        if (values.size != values.toSet().size) {
            throw BusinessException("ai model capabilities contain duplicate values")
        }
        return values.toSet()
    }

    @JsonIgnore
    inline fun <reified T> getRequestConfigObject(): T = requestConfig.parseObject()
}
