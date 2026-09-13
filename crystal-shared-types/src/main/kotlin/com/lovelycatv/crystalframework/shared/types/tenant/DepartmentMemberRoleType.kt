/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.types.tenant

enum class DepartmentMemberRoleType(val typeId: Int) {
    MEMBER(0),
    ADMIN(1),
    SUPER_ADMIN(2);

    companion object {
        fun getById(id: Int): DepartmentMemberRoleType? {
            return entries.find { it.typeId == id }
        }
    }
}
