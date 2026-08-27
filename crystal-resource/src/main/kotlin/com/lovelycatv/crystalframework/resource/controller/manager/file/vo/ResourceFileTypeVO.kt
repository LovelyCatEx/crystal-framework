package com.lovelycatv.crystalframework.resource.controller.manager.file.vo

import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility

/**
 * Wire-format for a single [com.lovelycatv.crystalframework.sdk.resource.file.types.ResourceFileTypeDeclaration].
 * Used by the admin UI to render the file-type dropdown so third-party contributions
 * (typeId >= 1000) show up on equal footing with the built-in enum entries. `typeId` fits in Int
 * so it stays as a number on the wire (no ToStringSerializer). `supportedContentTypes` and
 * `supportedFileExtensions` are the declaration's built-in defaults — deployment-time YAML
 * overrides (`crystalframework.resource.file-type-overrides.<key>`) are NOT reflected here; the
 * UI needs the declaration-level defaults for its schema hints, not the effective runtime set.
 */
data class ResourceFileTypeVO(
    val typeId: Int,
    val key: String,
    val displayName: String,
    val description: String,
    val supportedContentTypes: Set<String>,
    val supportedFileExtensions: Set<String>,
    val defaultVisibility: ResourceVisibility,
)
