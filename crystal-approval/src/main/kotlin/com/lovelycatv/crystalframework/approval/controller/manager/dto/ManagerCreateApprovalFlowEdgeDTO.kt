/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.controller.manager.dto

class ManagerCreateApprovalFlowEdgeDTO(
    var definitionId: Long = 0,
    var definitionVersion: Int = 0,
    var sourceNodeId: Long = 0,
    var targetNodeId: Long = 0,
)
