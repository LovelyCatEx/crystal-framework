package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerCreateScopedDTO

class ManagerCreateApprovalFlowInstanceDTO(
    override val scope: Int = 0,
    override val scopeId: Long = 0,
    var definitionId: Long = 0,
    var definitionVersion: Int = 0,
    var initiatorId: Long = 0,
    var formData: String? = null,
    var latestNodeId: Long = 0,
) : BaseManagerCreateScopedDTO(scope, scopeId)
