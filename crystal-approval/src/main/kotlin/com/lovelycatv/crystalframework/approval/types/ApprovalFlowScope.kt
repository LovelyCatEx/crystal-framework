/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.types

enum class ApprovalFlowScope(val typeId: Int) {
    SYSTEM(0),
    TENANT(1);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}
