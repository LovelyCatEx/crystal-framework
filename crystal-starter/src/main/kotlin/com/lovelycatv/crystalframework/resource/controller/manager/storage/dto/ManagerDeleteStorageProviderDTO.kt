package com.lovelycatv.crystalframework.resource.controller.manager.storage.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteStorageProviderDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
