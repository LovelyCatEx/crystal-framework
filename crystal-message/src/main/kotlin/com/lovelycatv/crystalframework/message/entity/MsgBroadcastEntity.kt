package com.lovelycatv.crystalframework.message.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.message.types.BroadcastCategory
import com.lovelycatv.crystalframework.sdk.message.types.AudienceType
import com.lovelycatv.crystalframework.sdk.message.types.PartyType
import com.lovelycatv.crystalframework.sdk.message.types.ScopeType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * A broadcast, written once (read-fanout). This is a generic broadcast base:
 * [category] is the business discriminator (announcement, ...), so new broadcast
 * businesses reuse this table rather than adding one. Recipients are resolved
 * lazily from [audienceType]/[audienceRef]; read state is tracked lazily in
 * [MsgBroadcastReadEntity].
 */
@Table("msg_broadcasts")
class MsgBroadcastEntity(
    id: Long = 0,
    @Column(value = "scope_type")
    var scopeType: Int = ScopeType.SYSTEM.typeId,
    @Column(value = "scope_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var scopeId: Long? = null,
    @Column(value = "sender_party_type")
    var senderPartyType: Int = PartyType.SYSTEM.typeId,
    @Column(value = "sender_party_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var senderPartyId: Long? = null,
    @Column(value = "acting_user_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var actingUserId: Long? = null,
    @Column(value = "category")
    var category: Int = BroadcastCategory.ANNOUNCEMENT.typeId,
    @Column(value = "audience_type")
    var audienceType: Int = AudienceType.ALL_USERS.typeId,
    @Column(value = "audience_ref")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var audienceRef: Long? = null,
    @Column(value = "title")
    var title: String = "",
    @Column(value = "content")
    var content: String = "",
    @Column(value = "publish_time")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var publishTime: Long = System.currentTimeMillis(),
    @Column(value = "expire_time")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var expireTime: Long? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getRealScopeType(): ScopeType =
        ScopeType.getByTypeId(this.scopeType)
            ?: throw BusinessException("broadcast scope type ${this.scopeType} not found")

    @JsonIgnore
    fun getRealSenderPartyType(): PartyType =
        PartyType.getByTypeId(this.senderPartyType)
            ?: throw BusinessException("broadcast sender party type ${this.senderPartyType} not found")

    @JsonIgnore
    fun getRealCategory(): BroadcastCategory =
        BroadcastCategory.getByTypeId(this.category)
            ?: throw BusinessException("broadcast category ${this.category} not found")

    @JsonIgnore
    fun getRealAudienceType(): AudienceType =
        AudienceType.getByTypeId(this.audienceType)
            ?: throw BusinessException("broadcast audience type ${this.audienceType} not found")
}
