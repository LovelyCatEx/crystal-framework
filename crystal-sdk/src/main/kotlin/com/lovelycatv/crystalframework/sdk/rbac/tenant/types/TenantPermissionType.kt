/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.rbac.tenant.types

enum class TenantPermissionType(val typeId: Int) {
    ACTION(0),
    MENU(1);

    companion object {
        fun getById(id: Int): TenantPermissionType? {
            return entries.find { it.typeId == id }
        }
    }
}
