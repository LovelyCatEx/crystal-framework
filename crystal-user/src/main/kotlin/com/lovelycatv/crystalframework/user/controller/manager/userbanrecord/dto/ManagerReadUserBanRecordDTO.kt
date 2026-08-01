package com.lovelycatv.crystalframework.user.controller.manager.userbanrecord.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadUserBanRecordDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val userId: Long? = null,
    val operatorUserId: Long? = null
) : BaseManagerReadDTO(page, pageSize)
