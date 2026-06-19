package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerCreateScopedDTO

class ManagerCreateApprovalFlowTaskDTO(
    override val scope: Int = 0,
    override val scopeId: Long = 0,
    var instanceId: Long = 0,
    var nodeId: Long = 0,
    var assigneeId: Long = 0,
    var formData: String? = null,
) : BaseManagerCreateScopedDTO(scope, scopeId)
