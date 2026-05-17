package com.lovelycatv.crystalframework.resource.controller.manager.file.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ManagerUpdateFileResourceDTO(
    override val id: Long,
    val userId: Long? = null,
    val type: Int? = null,

    @field:Size(max = 256, message = "File name length cannot exceed 256 characters")
    val fileName: String? = null,

    @field:Size(max = 64, message = "File extension length cannot exceed 64 characters")
    val fileExtension: String? = null,

    @field:Pattern(regexp = "^[a-fA-F0-9]{32}$", message = "MD5 must be 32 hexadecimal characters")
    val md5: String? = null,

    val fileSize: Long? = null,
    val storageProviderId: Long? = null,

    @field:Size(max = 256, message = "Object key length cannot exceed 256 characters")
    val objectKey: String? = null
) : BaseManagerUpdateDTO(id)
