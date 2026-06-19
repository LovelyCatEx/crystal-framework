package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

class ManagerUpdateApprovalFlowEdgeDTO(
    override val id: Long = 0,
    var sourceNodeId: Long? = null,
    var targetNodeId: Long? = null,
) : BaseManagerUpdateDTO(id)
