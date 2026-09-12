package com.lovelycatv.crystalframework.ai.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.ai.types.AiModelInvocationStatus
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("ai_model_invocation_records")
class AiModelInvocationRecordEntity(
    id: Long = 0,

    @Column("request_id")
    var requestId: String = "",
    @Column("user_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var userId: Long = 0,
    @Column("tenant_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var tenantId: Long? = null,
    @Column("session_id")
    var sessionId: String? = null,
    @Column("provider_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var providerId: Long = 0,
    @Column("model_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var modelId: Long = 0,

    @Column("prompt_tokens")
    var promptTokens: Int = 0,
    @Column("cached_prompt_tokens")
    var cachedPromptTokens: Int = 0,
    @Column("completion_tokens")
    var completionTokens: Int = 0,
    @Column("reasoning_tokens")
    var reasoningTokens: Int = 0,
    @Column("cache_creation_tokens")
    var cacheCreationTokens: Int = 0,

    @Column("tool_calls_count")
    var toolCallsCount: Int = 0,
    @Column("message_count")
    var messageCount: Int = 0,
    @Column("is_streaming")
    var isStreaming: Boolean = false,

    @Column("time_to_first_token_ms")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var timeToFirstTokenMs: Long = 0,
    @Column("total_duration_ms")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var totalDurationMs: Long = 0,
    @Column("queue_wait_ms")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var queueWaitMs: Long? = null,
    @Column("tokens_per_second")
    var tokensPerSecond: Double? = null,

    @Column("prompt_unit_price")
    var promptUnitPrice: Double = 0.0,
    @Column("completion_unit_price")
    var completionUnitPrice: Double = 0.0,
    @Column("cache_read_unit_price")
    var cacheReadUnitPrice: Double = 0.0,
    @Column("cache_write_unit_price")
    var cacheWriteUnitPrice: Double = 0.0,
    @Column("group_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var groupId: Long? = null,
    @Column("group_multiplier")
    var groupMultiplier: Double = 1.0,
    @Column("raw_cost")
    var rawCost: Double = 0.0,
    @Column("final_cost")
    var finalCost: Double = 0.0,
    @Column("currency")
    var currency: String = "USD",

    @Column("temperature")
    var temperature: Double? = null,
    @Column("top_p")
    var topP: Double? = null,
    @Column("max_tokens")
    var maxTokens: Int? = null,

    @Column("status")
    var status: Int = AiModelInvocationStatus.SUCCESS.typeId,
    @Column("error_code")
    var errorCode: String? = null,
    @Column("error_message")
    var errorMessage: String? = null,
    @Column("stop_reason")
    var stopReason: String? = null,

    @Column("client_ip")
    var clientIp: String? = null,
    @Column("user_agent")
    var userAgent: String? = null,

    @Column("request_size_bytes")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var requestSizeBytes: Long? = null,
    @Column("response_size_bytes")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var responseSizeBytes: Long? = null,

    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getRealStatus(): AiModelInvocationStatus = AiModelInvocationStatus.getByTypeId(status)
        ?: throw BusinessException("Invalid invocation status: $status")
}
