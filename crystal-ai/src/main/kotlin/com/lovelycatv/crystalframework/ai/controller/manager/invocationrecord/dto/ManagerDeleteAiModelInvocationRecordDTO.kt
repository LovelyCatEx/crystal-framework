package com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteAiModelInvocationRecordDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
