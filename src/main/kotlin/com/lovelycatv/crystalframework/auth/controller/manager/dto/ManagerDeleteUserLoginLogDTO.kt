package com.lovelycatv.crystalframework.auth.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteUserLoginLogDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)