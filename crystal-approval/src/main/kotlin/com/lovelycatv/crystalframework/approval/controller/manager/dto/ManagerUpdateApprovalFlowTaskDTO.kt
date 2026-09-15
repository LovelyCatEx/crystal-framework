/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

class ManagerUpdateApprovalFlowTaskDTO(
    override val id: Long = 0,
    var status: Int? = null,
    var comment: String? = null,
    var formData: String? = null,
) : BaseManagerUpdateDTO(id)
