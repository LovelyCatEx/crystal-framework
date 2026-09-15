/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.controller.manager.dto

class ManagerCreateApprovalFlowNodeDTO(
    var definitionId: Long = 0,
    var definitionVersion: Int = 0,
    var nodeKey: String = "",
    var type: Int = 0,
    var name: String = "",
    var config: String? = null,
    var formSchema: String? = null,
    var positionX: Int = 0,
    var positionY: Int = 0,
)
