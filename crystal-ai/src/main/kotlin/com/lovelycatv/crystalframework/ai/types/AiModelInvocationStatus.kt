package com.lovelycatv.crystalframework.ai.types

enum class AiModelInvocationStatus(val typeId: Int) {
    FAILED(0),
    SUCCESS(1);

    companion object {
        fun getByTypeId(typeId: Int): AiModelInvocationStatus? = entries.find { it.typeId == typeId }
    }
}
