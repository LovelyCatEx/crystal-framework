package com.lovelycatv.crystalframework.message.controller.vo

import com.lovelycatv.crystalframework.message.entity.MsgBroadcastEntity
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * Outward-facing view of a broadcast in the user's history inbox: the serialized broadcast fields
 * plus [read] (whether the querying user has read it). Mirrors [MsgBroadcastEntity]'s wire shape so
 * the frontend `Broadcast` type stays reusable, with [read] added. All `Long` fields are emitted as
 * `String` per the Long-serialization rule (nullable `Long?` needs the explicit getter serializer).
 */
data class BroadcastInboxVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    val id: Long,
    val scopeType: Int,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val scopeId: Long?,
    val senderPartyType: Int,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val senderPartyId: Long?,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val actingUserId: Long?,
    val category: Int,
    val audienceType: Int,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val audienceRef: Long?,
    val title: String,
    val content: String,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val publishTime: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val expireTime: Long?,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val createdTime: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val modifiedTime: Long,
    val read: Boolean,
) {
    companion object {
        fun of(entity: MsgBroadcastEntity, read: Boolean): BroadcastInboxVO = BroadcastInboxVO(
            id = entity.id,
            scopeType = entity.scopeType,
            scopeId = entity.scopeId,
            senderPartyType = entity.senderPartyType,
            senderPartyId = entity.senderPartyId,
            actingUserId = entity.actingUserId,
            category = entity.category,
            audienceType = entity.audienceType,
            audienceRef = entity.audienceRef,
            title = entity.title,
            content = entity.content,
            publishTime = entity.publishTime,
            expireTime = entity.expireTime,
            createdTime = entity.createdTime,
            modifiedTime = entity.modifiedTime,
            read = read,
        )
    }
}
