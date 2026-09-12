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
