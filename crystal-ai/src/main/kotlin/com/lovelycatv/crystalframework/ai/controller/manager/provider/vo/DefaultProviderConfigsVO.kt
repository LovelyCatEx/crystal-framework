/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.controller.manager.provider.vo

import com.lovelycatv.vertex.ai.llm.config.LLMResponseConfig

data class DefaultProviderConfigsVO(
    val openai: LLMResponseConfig,
    val anthropic: LLMResponseConfig,
)
