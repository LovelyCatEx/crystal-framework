/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

class ManagerUpdateApprovalFlowDefinitionDTO(
    override val id: Long = 0,
    var name: String? = null,
    var description: String? = null,
    var status: Int? = null,
    var formSchema: String? = null,
) : BaseManagerUpdateDTO(id)
