/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.types

enum class ApprovalFlowNodeType(val typeId: Int) {
    START(0),
    END(1),
    APPROVAL(2),
    CONDITION(3),
    CC(4),
    FORK(5),
    JOIN(6);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}
