package com.lovelycatv.crystalframework.resource.controller.manager.file.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteFileResourceDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
