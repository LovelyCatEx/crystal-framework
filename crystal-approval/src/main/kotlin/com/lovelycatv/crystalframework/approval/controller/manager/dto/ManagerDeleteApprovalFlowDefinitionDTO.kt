package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

class ManagerDeleteApprovalFlowDefinitionDTO(
    override val ids: List<Long> = emptyList(),
) : BaseManagerDeleteDTO(ids)
