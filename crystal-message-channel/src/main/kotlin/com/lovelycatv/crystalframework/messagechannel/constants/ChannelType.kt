package com.lovelycatv.crystalframework.messagechannel.constants

enum class ChannelType(val typeId: Int) {
    EMAIL(1),
    FEISHU(2);

    companion object {
        fun fromTypeId(typeId: Int): ChannelType? = entries.firstOrNull { it.typeId == typeId }
    }
}
