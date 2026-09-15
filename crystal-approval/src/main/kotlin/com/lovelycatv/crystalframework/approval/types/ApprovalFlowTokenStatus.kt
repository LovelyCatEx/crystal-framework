/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.types

enum class ApprovalFlowTokenStatus(val typeId: Int) {
    ACTIVE(0),
    WAITING(1),
    COMPLETED(2);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}
