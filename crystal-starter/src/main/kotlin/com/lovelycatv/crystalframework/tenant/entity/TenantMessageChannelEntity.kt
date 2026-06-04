package com.lovelycatv.crystalframework.tenant.entity

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("tenant_message_channels")
class TenantMessageChannelEntity(
    id: Long = 0,
    tenantId: Long = 0,
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
) : BaseTenantEntity(id, tenantId, createdTime, modifiedTime, deletedTime) {

    fun getRealChannelType(): ChannelType =
        ChannelType.fromTypeId(channelType)
            ?: error("Unknown channelType=$channelType for channel id=$id")
}
