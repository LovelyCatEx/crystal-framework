/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.service

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity
import com.lovelycatv.crystalframework.approval.types.CcNodeConfig

/**
 * Sends a carbon-copy notification for a CC node in an approval flow.
 *
 * Failure is intentionally swallowed inside the implementation and logged, so a broken
 * channel never blocks the flow — CC is informational, not gating.
 */
interface ApprovalCcNotifier {
    suspend fun notify(
        instance: ApprovalFlowInstanceEntity,
        node: ApprovalFlowNodeEntity,
        config: CcNodeConfig,
    )
}
