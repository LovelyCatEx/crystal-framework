package com.lovelycatv.crystalframework.user.entity

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("user_ban_records")
class UserBanRecordEntity(
    id: Long = 0,
    @Column(value = "user_id")
    var userId: Long = 0,
    @Column(value = "reason")
    var reason: String = "",
    @Column(value = "ban_until")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var banUntil: Long? = null,
    @Column(value = "lifted_time")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var liftedTime: Long? = null,
    @Column(value = "operator_user_id")
    var operatorUserId: Long = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
