package com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateAiUserGroupMemberDTO(
    override val id: Long,
    val userGroupId: Long? = null,
    val userId: Long? = null
) : BaseManagerUpdateDTO(id)
