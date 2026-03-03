package com.lovelycatv.crystalframework.resource.controller.manager.storage.dto

data class ManagerCreateStorageProviderDTO(
    val name: String,
    val description: String? = null,
    val type: Int,
    val baseUrl: String,
    val properties: String
)
