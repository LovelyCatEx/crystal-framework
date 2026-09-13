/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.types.tenant

enum class TenantStatus(val typeId: Int) {
    REVIEWING(0),
    ACTIVE(1),
    CLOSED(2);

    companion object {
        fun getTenantStatusType(typeId: Int): TenantStatus? {
            return entries.find { it.typeId == typeId }
        }
    }
}