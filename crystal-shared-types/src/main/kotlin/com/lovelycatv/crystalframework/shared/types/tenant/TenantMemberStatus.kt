/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.types.tenant

enum class TenantMemberStatus(val typeId: Int) {
    INACTIVE(0),
    DEPARTED(1),
    RESIGNED(2),
    REVIEWING(3),
    ACTIVE(4);

    companion object {
        fun getByType(typeId: Int): TenantMemberStatus? {
            return entries.find { it.typeId == typeId }
        }
    }
}