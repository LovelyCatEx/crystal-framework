package com.lovelycatv.crystalframework.resource.controller.manager.routing.vo

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * Minimal provider view for simulate result — only what the admin needs to identify which
 * provider was picked. Full provider fields are not required here; if callers need them they can
 * hit the storage-provider controller with the returned id.
 */
data class StorageProviderSimpleVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    val id: Long,
    val name: String,
)
