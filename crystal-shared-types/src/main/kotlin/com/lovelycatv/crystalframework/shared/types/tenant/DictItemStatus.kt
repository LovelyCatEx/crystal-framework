/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.types.tenant

enum class DictItemStatus(val typeId: Int) {
    DISABLED(0),
    ENABLED(1);

    companion object {
        fun getById(id: Int): DictItemStatus? {
            return entries.find { it.typeId == id }
        }
    }
}
