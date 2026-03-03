package com.lovelycatv.crystalframework.resource.controller.manager.storage.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateStorageProviderDTO(
    override val id: Long,
    val name: String? = null,
    val description: String? = null,
    val type: Int? = null,
    val baseUrl: String? = null,
    val properties: String? = null,
    val active: Boolean? = null,
) : BaseManagerUpdateDTO(id)
