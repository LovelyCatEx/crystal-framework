package com.lovelycatv.crystalframework.ai.controller.manager.playground.vo

/**
 * Everything the playground needs to render its tree and group picker, resolved down to what the
 * caller's user groups grant. Keys are `Long` ids serialized to strings.
 */
data class ManagerAiPlaygroundDataVO(
    val providers: Map<String, ManagerAiPlaygroundProviderVO>,
    val groups: Map<String, ManagerAiPlaygroundGroupVO>,
    val models: Map<String, ManagerAiPlaygroundModelVO>,
)
