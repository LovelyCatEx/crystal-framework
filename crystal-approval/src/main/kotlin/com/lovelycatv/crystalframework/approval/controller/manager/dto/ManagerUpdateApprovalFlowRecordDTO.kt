package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

class ManagerUpdateApprovalFlowRecordDTO(
    override val id: Long = 0,
    var action: Int? = null,
    var comment: String? = null,
) : BaseManagerUpdateDTO(id)
