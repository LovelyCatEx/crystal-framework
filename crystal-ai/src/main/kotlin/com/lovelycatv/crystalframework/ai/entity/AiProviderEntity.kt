package com.lovelycatv.crystalframework.ai.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.ai.types.AiProviderProtocolType
import com.lovelycatv.crystalframework.shared.annotations.NotQueryable
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.utils.parseObject
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("ai_providers")
class AiProviderEntity(
    id: Long = 0,
    @Column("name")
    var name: String = "",
    @Column("key")
    var key: String = "",
    @Column("description")
    var description: String? = null,
    @Column("protocol_type")
    var protocolType: Int = AiProviderProtocolType.OPENAI_COMPATIBLE.typeId,
    @Column("base_url")
    var baseUrl: String = "",
    @Column("api_key")
    @field:NotQueryable
    var apiKey: String = "",
    @Column("chat_completions_path")
    var chatCompletionsPath: String? = null,
    @Column("embedding_path")
    var embeddingPath: String? = null,
    @Column("request_config")
    @field:NotQueryable
    var requestConfig: String = "{}",
    @Column("response_config")
    @field:NotQueryable
    var responseConfig: String = "{}",
    @Column("enabled")
    var enabled: Boolean = true,
    @Column("sort")
    var sort: Int = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getRealProtocolType(): AiProviderProtocolType = AiProviderProtocolType.getByTypeId(protocolType)
        ?: throw BusinessException("ai provider protocol type $protocolType not found")

    @JsonIgnore
    inline fun <reified T> getRequestConfigObject(): T = requestConfig.parseObject()

    @JsonIgnore
    inline fun <reified T> getResponseConfigObject(): T = responseConfig.parseObject()
}
