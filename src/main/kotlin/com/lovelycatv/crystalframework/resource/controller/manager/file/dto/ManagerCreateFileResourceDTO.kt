package com.lovelycatv.crystalframework.resource.controller.manager.file.dto

data class ManagerCreateFileResourceDTO(
    val userId: Long,
    val type: Int,
    val fileName: String,
    val fileExtension: String,
    val md5: String,
    val fileSize: Long,
    val storageProviderId: Long,
    val objectKey: String
)
