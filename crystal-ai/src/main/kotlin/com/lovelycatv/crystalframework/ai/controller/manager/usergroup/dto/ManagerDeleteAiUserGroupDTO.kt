package com.lovelycatv.crystalframework.ai.controller.manager.usergroup.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteAiUserGroupDTO(
    override val ids: List<Long>,
) : BaseManagerDeleteDTO(ids)
