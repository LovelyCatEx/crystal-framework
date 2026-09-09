package com.lovelycatv.crystalframework.ai.controller.manager.provider.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteAiProviderDTO(
    override val ids: List<Long>,
) : BaseManagerDeleteDTO(ids)
