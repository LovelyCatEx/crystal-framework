/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.types

enum class ApprovalFlowInstanceStatus(val typeId: Int) {
    IN_PROGRESS(0),
    APPROVED(1),
    REJECTED(2),
    CANCELLED(3);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}
