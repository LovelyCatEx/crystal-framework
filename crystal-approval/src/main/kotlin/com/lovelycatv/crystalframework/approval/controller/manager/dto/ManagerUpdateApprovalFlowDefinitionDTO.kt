package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

class ManagerUpdateApprovalFlowDefinitionDTO(
    override val id: Long = 0,
    var name: String? = null,
    var description: String? = null,
    var status: Int? = null,
    var formSchema: String? = null,
) : BaseManagerUpdateDTO(id)
