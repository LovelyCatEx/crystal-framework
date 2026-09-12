package com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateAiUserGroupModelDTO(
    override val id: Long,
    val userGroupId: Long? = null,
    val modelId: Long? = null
) : BaseManagerUpdateDTO(id)
