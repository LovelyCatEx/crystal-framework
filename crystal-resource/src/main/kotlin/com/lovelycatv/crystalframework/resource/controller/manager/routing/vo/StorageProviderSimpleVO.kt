/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
