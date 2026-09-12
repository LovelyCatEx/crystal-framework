package com.lovelycatv.crystalframework.ai.controller.manager.provider.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

data class ManagerUpdateAiProviderDTO(
    override val id: Long,
    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String? = null,
    @field:Size(max = 128, message = "Key length cannot exceed 128 characters")
    val key: String? = null,
    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,
    val protocolType: Int? = null,
    @field:Size(max = 512, message = "Base URL length cannot exceed 512 characters")
    val baseUrl: String? = null,
    val apiKey: String? = null,
    @field:Size(max = 256, message = "Chat completions path length cannot exceed 256 characters")
    val chatCompletionsPath: String? = null,
    @field:Size(max = 256, message = "Embedding path length cannot exceed 256 characters")
    val embeddingPath: String? = null,
    val requestConfig: String? = null,
    val responseConfig: String? = null,
    val enabled: Boolean? = null,
    val sort: Int? = null,
) : BaseManagerUpdateDTO(id)
