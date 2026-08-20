package com.lovelycatv.crystalframework.message.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.sdk.message.types.PartyType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * A party seated in a conversation (the outward-facing "who"). DIRECT holds two
 * rows; a group conversation holds a single group-like party row.
 */
@Table("msg_conversation_parties")
class MsgConversationPartyEntity(
    id: Long = 0,
    @Column(value = "conversation_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var conversationId: Long = 0,
    @Column(value = "party_type")
    var partyType: Int = PartyType.USER.typeId,
    @Column(value = "party_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var partyId: Long? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getRealPartyType(): PartyType =
        PartyType.getByTypeId(this.partyType)
            ?: throw BusinessException("conversation party type ${this.partyType} not found")
}
