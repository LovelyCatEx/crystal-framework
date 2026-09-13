/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
