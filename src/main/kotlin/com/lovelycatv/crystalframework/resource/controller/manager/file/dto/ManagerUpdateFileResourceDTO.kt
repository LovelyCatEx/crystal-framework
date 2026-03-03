package com.lovelycatv.crystalframework.resource.controller.manager.file.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateFileResourceDTO(
    override val id: Long,
    val userId: Long? = null,
    val type: Int? = null,
    val fileName: String? = null,
    val fileExtension: String? = null,
    val md5: String? = null,
    val fileSize: Long? = null,
    val storageProviderId: Long? = null,
    val objectKey: String? = null
) : BaseManagerUpdateDTO(id)
