package com.lovelycatv.crystalframework.system.controller.manager.announcement.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

class ManagerUpdateAnnouncementDTO(
    override val id: Long = 0,
    @field:Size(max = 256)
    val title: String? = null,
    val content: String? = null,
    val status: Int? = null,
    val target: Int? = null,
    val priority: Int? = null,
) : BaseManagerUpdateDTO(id)
