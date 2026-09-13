/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerCreateScopedDTO

class ManagerCreateApprovalFlowRecordDTO(
    override val scope: Int = 0,
    override val scopeId: Long = 0,
    var instanceId: Long = 0,
    var nodeId: Long = 0,
    var operatorId: Long = 0,
    var action: Int = 0,
    var comment: String? = null,
) : BaseManagerCreateScopedDTO(scope, scopeId)
