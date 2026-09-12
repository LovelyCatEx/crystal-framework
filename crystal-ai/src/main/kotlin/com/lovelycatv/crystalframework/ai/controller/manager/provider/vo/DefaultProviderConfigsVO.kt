package com.lovelycatv.crystalframework.ai.controller.manager.provider.vo

import com.lovelycatv.crystalframework.ai.types.AiProviderResponseConfig

data class DefaultProviderConfigsVO(
    val openai: AiProviderResponseConfig,
    val anthropic: AiProviderResponseConfig,
)
