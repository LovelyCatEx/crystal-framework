/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

class ManagerUpdateApprovalFlowNodeDTO(
    override val id: Long = 0,
    var nodeKey: String? = null,
    var type: Int? = null,
    var name: String? = null,
    var config: String? = null,
    var formSchema: String? = null,
    var positionX: Int? = null,
    var positionY: Int? = null,
) : BaseManagerUpdateDTO(id)
