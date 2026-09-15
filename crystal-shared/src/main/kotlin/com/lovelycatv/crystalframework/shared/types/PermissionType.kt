/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.types

enum class PermissionType(val typeId: Int) {
    ACTION(0),
    MENU(1),
    COMPONENT(2),;

    companion object {
        fun getById(id: Int): PermissionType? {
            return entries.find { it.typeId == id }
        }
    }
}