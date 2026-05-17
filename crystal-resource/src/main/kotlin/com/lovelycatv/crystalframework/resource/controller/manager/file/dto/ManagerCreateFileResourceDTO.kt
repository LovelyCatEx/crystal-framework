package com.lovelycatv.crystalframework.resource.controller.manager.file.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ManagerCreateFileResourceDTO(
    @field:NotNull(message = "User ID is required")
    val userId: Long,

    @field:NotNull(message = "Type is required")
    val type: Int,

    @field:NotBlank(message = "File name is required")
    @field:Size(max = 256, message = "File name length cannot exceed 256 characters")
    val fileName: String,

    @field:NotBlank(message = "File extension is required")
    @field:Size(max = 64, message = "File extension length cannot exceed 64 characters")
    val fileExtension: String,

    @field:NotBlank(message = "MD5 is required")
    @field:Pattern(regexp = "^[a-fA-F0-9]{32}$", message = "MD5 must be 32 hexadecimal characters")
    val md5: String,

    @field:NotNull(message = "File size is required")
    val fileSize: Long,

    @field:NotNull(message = "Storage provider ID is required")
    val storageProviderId: Long,

    @field:NotBlank(message = "Object key is required")
    @field:Size(max = 256, message = "Object key length cannot exceed 256 characters")
    val objectKey: String
)
