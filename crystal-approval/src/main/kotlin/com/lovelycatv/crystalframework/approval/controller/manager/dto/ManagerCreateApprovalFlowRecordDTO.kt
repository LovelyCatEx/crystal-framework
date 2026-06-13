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
