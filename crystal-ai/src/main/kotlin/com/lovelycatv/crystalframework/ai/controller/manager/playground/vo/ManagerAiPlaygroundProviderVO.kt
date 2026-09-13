package com.lovelycatv.crystalframework.ai.controller.manager.playground.vo

/**
 * A provider as shown in the playground: only its display name and the enabled models that hang
 * under it. Credentials, base URL and request/response config are deliberately not exposed.
 */
data class ManagerAiPlaygroundProviderVO(
    val name: String,
    val modelIds: List<String>,
)
