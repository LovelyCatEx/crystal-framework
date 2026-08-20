package com.lovelycatv.crystalframework.message.entity

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * A lazily-written read marker for a broadcast: a row exists only once a user
 * has actually read the broadcast. Absence of a row means unread.
 */
@Table("msg_broadcast_reads")
class MsgBroadcastReadEntity(
    id: Long = 0,
    @Column(value = "broadcast_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var broadcastId: Long = 0,
    @Column(value = "user_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var userId: Long = 0,
    @Column(value = "read_time")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var readTime: Long = System.currentTimeMillis(),
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
