package com.lovelycatv.crystalframework.ai.controller.manager.provider.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ManagerCreateAiProviderDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String,
    @field:NotBlank(message = "Key is required")
    @field:Size(max = 128, message = "Key length cannot exceed 128 characters")
    val key: String,
    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,
    @field:NotNull(message = "Protocol type is required")
    val protocolType: Int,
    @field:NotBlank(message = "Base URL is required")
    @field:Size(max = 512, message = "Base URL length cannot exceed 512 characters")
    val baseUrl: String,
    @field:NotBlank(message = "API key is required")
    val apiKey: String,
    @field:Size(max = 256, message = "Chat completions path length cannot exceed 256 characters")
    val chatCompletionsPath: String? = null,
    @field:Size(max = 256, message = "Embedding path length cannot exceed 256 characters")
    val embeddingPath: String? = null,
    @field:NotBlank(message = "Request config is required")
    val requestConfig: String = "{}",
    @field:NotBlank(message = "Response config is required")
    val responseConfig: String = "{}",
    val enabled: Boolean = true,
    val sort: Int = 0,
)
