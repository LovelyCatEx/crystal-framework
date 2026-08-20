package com.lovelycatv.crystalframework.message.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.message.types.ContentType
import com.lovelycatv.crystalframework.sdk.message.types.PartyType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * A single message, stored once per conversation (never fanned out per recipient).
 * [senderPartyType]/[senderPartyId] is the outward-facing sender; [actingUserId] is
 * the real user who actually sent it (null = system auto-sent on a party's behalf).
 */
@Table("msg_messages")
class MsgMessageEntity(
    id: Long = 0,
    @Column(value = "conversation_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var conversationId: Long = 0,
    @Column(value = "sender_party_type")
    var senderPartyType: Int = PartyType.USER.typeId,
    @Column(value = "sender_party_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var senderPartyId: Long? = null,
    @Column(value = "acting_user_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var actingUserId: Long? = null,
    @Column(value = "content_type")
    var contentType: Int = ContentType.TEXT.typeId,
    @Column(value = "content")
    var content: String = "",
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getRealSenderPartyType(): PartyType =
        PartyType.getByTypeId(this.senderPartyType)
            ?: throw BusinessException("message sender party type ${this.senderPartyType} not found")

    @JsonIgnore
    fun getRealContentType(): ContentType =
        ContentType.getByTypeId(this.contentType)
            ?: throw BusinessException("message content type ${this.contentType} not found")
}
