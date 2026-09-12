package com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteAiUserGroupModelDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
