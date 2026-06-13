package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

class ManagerUpdateApprovalFlowTaskDTO(
    override val id: Long = 0,
    var status: Int? = null,
    var comment: String? = null,
    var formData: String? = null,
) : BaseManagerUpdateDTO(id)
