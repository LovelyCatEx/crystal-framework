/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.types

enum class ApprovalFlowApproverStrategy(val typeId: Int) {
    SPECIFIED_USER(0),
    SPECIFIED_ROLE(1),
    DIRECT_SUPERIOR(2),
    DEPARTMENT_HEAD(3),
    INITIATOR_CHOOSE(4);

    companion object {
        fun getById(id: Int) = entries.firstOrNull { it.typeId == id }
    }
}
