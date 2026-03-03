package com.lovelycatv.crystalframework.user.controller.manager.oauth.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteOAuthAccountDTO(
    override val id: Long
) : BaseManagerDeleteDTO(id)
