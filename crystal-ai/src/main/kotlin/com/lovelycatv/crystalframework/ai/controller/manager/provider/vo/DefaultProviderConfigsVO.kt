package com.lovelycatv.crystalframework.ai.controller.manager.provider.vo

import com.lovelycatv.vertex.ai.llm.config.LLMResponseConfig

data class DefaultProviderConfigsVO(
    val openai: LLMResponseConfig,
    val anthropic: LLMResponseConfig,
)
