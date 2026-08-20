package com.lovelycatv.crystalframework.message.entity

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * A materialized real-user membership of a conversation, plus that user's read
 * cursor. This is the reverse index (userId -> conversations) that powers the
 * inbox; for SHARED read granularity a party may collapse to a single row.
 */
@Table("msg_conversation_members")
class MsgConversationMemberEntity(
    id: Long = 0,
    @Column(value = "conversation_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var conversationId: Long = 0,
    @Column(value = "user_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var userId: Long = 0,
    @Column(value = "last_read_message_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var lastReadMessageId: Long? = null,
    @Column(value = "unread_count")
    var unreadCount: Int = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
