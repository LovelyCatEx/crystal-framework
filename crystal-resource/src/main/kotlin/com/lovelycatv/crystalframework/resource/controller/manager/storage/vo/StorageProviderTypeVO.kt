package com.lovelycatv.crystalframework.resource.controller.manager.storage.vo

/**
 * Wire-format for a single [com.lovelycatv.crystalframework.sdk.resource.storage.types.StorageProviderTypeDeclaration].
 * Used by the admin UI to render the storage-provider type dropdown so third-party contributions
 * (typeId >= 1000) show up on equal footing with the built-in enum entries. `typeId` fits in Int
 * so it stays as a number on the wire (no ToStringSerializer).
 */
data class StorageProviderTypeVO(
    val typeId: Int,
    val key: String,
    val displayName: String,
    val description: String,
)
