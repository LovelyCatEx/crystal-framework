package com.lovelycatv.crystalframework.user.controller.manager.userbanrecord.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteUserBanRecordDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
