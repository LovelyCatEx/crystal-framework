package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerCreateScopedDTO

class ManagerCreateApprovalFlowDefinitionDTO(
    override val scope: Int = 0,
    override val scopeId: Long = 0,
    var name: String = "",
    var description: String? = null,
    var formSchema: String? = null,
) : BaseManagerCreateScopedDTO(scope, scopeId)
