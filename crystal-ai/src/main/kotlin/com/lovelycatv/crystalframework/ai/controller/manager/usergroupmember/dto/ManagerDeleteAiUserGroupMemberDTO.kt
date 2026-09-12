package com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteAiUserGroupMemberDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
