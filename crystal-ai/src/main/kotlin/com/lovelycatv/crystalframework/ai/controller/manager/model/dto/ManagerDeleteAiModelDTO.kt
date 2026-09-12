package com.lovelycatv.crystalframework.ai.controller.manager.model.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteAiModelDTO(
    override val ids: List<Long>,
) : BaseManagerDeleteDTO(ids)
