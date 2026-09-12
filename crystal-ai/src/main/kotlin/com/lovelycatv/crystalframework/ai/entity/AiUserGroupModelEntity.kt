package com.lovelycatv.crystalframework.ai.entity

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("ai_user_group_models")
class AiUserGroupModelEntity(
    id: Long = 0,
    @Column("user_group_id")
    var userGroupId: Long = 0,
    @Column("model_id")
    var modelId: Long = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
