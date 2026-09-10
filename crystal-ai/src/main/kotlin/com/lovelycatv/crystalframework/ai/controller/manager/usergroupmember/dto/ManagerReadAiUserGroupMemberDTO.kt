package com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadAiUserGroupMemberDTO(
    override val page: Int = 1,
    override val pageSize: Int = 20,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val userGroupId: Long? = null,
    val userId: Long? = null
) : BaseManagerReadDTO(page, pageSize)
