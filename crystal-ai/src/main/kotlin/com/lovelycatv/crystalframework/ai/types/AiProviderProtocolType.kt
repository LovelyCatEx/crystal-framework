/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.types

enum class AiProviderProtocolType(
    val typeId: Int,
) {
    OPENAI_COMPATIBLE(0),
    ANTHROPIC_MESSAGES(1);

    companion object {
        fun getByTypeId(typeId: Int): AiProviderProtocolType? = entries.firstOrNull { it.typeId == typeId }
    }
}
