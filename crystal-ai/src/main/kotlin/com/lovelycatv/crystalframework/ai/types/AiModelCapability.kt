package com.lovelycatv.crystalframework.ai.types

enum class AiModelCapability(
    val typeId: Int,
) {
    CHAT(0),
    TEXT_GENERATION(1),
    VISION(2),
    EMBEDDING(3),
    TOOL_CALLING(4),
    STRUCTURED_OUTPUT(5),
    AUDIO_INPUT(6),
    AUDIO_OUTPUT(7);

    companion object {
        fun getByTypeId(typeId: Int): AiModelCapability? = entries.firstOrNull { it.typeId == typeId }
    }
}
