/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.types.common

/**
 * Defines the scope level that a resource belongs to.
 *
 * - [SYSTEM]: System-level resource, shared globally.
 * - [TENANT]: Tenant-level resource, isolated per tenant.
 *
 * [typeId] is the serialized value stored/transmitted in DTOs.
 */
enum class ResourceScope(val typeId: Int) {
    SYSTEM(0),
    TENANT(1);

    companion object {
        fun getById(typeId: Int): ResourceScope? = entries.firstOrNull { it.typeId == typeId }
    }
}
