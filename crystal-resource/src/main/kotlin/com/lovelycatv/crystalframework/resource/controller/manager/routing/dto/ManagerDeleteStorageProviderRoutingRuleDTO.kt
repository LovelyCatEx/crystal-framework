package com.lovelycatv.crystalframework.resource.controller.manager.routing.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteStorageProviderRoutingRuleDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
