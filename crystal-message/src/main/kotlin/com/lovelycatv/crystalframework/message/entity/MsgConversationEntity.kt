package com.lovelycatv.crystalframework.message.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.message.types.ConversationKind
import com.lovelycatv.crystalframework.sdk.message.types.ScopeType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * A conversation container. Isolation lives entirely in [dedupeKey] (scope segment
 * + kind + normalized parties); the core never stores a concrete tenant column.
 */
@Table("msg_conversations")
class MsgConversationEntity(
    id: Long = 0,
    @Column(value = "scope_type")
    var scopeType: Int = ScopeType.SYSTEM.typeId,
    @Column(value = "scope_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var scopeId: Long? = null,
    @Column(value = "conversation_kind")
    var conversationKind: Int = ConversationKind.DIRECT.typeId,
    @Column(value = "dedupe_key")
    var dedupeKey: String = "",
    @Column(value = "last_message_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var lastMessageId: Long? = null,
    @Column(value = "last_message_time")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var lastMessageTime: Long? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getRealScopeType(): ScopeType =
        ScopeType.getByTypeId(this.scopeType)
            ?: throw BusinessException("conversation scope type ${this.scopeType} not found")

    @JsonIgnore
    fun getRealConversationKind(): ConversationKind =
        ConversationKind.getByTypeId(this.conversationKind)
            ?: throw BusinessException("conversation kind ${this.conversationKind} not found")
}
