package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

class ManagerUpdateApprovalFlowInstanceDTO(
    override val id: Long = 0,
    var status: Int? = null,
    var currentNodeId: Long? = null,
    var formData: String? = null,
) : BaseManagerUpdateDTO(id)
