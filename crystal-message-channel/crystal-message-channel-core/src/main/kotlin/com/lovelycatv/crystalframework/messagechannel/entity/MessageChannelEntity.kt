package com.lovelycatv.crystalframework.messagechannel.entity

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.shared.types.entity.BaseScopedEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("message_channels")
class MessageChannelEntity(
    id: Long = 0,
    scope: Int = 0,
    scopeId: Long = 0,
    @Column("channel_type")
    var channelType: Int = 0,
    @Column("name")
    var name: String = "",
    @Column("enabled")
    var enabled: Boolean = true,
    @Column("config")
    var config: String = "{}",
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseScopedEntity(id, scope, scopeId, createdTime, modifiedTime, deletedTime) {

    fun getRealChannelType(): ChannelType =
        ChannelType.fromTypeId(channelType)
            ?: error("Unknown channelType=$channelType for channel id=$id")
}
