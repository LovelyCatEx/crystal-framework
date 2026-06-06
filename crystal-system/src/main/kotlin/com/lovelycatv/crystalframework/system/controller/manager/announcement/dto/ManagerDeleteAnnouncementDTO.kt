package com.lovelycatv.crystalframework.system.controller.manager.announcement.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

class ManagerDeleteAnnouncementDTO(
    override val ids: List<Long> = emptyList(),
) : BaseManagerDeleteDTO(ids)
