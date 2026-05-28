package com.lovelycatv.crystalframework.system.controller.manager.announcement.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadAnnouncementDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val status: Int? = null,
    val target: Int? = null,
) : BaseManagerReadDTO(page, pageSize)
