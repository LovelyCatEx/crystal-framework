package com.lovelycatv.crystalframework.approval.entity

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("approval_flow_edge")
class ApprovalFlowEdgeEntity(
    id: Long = 0,
    @Column("definition_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var definitionId: Long = 0,
    @Column("definition_version")
    var definitionVersion: Int = 0,
    @Column("source_node_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var sourceNodeId: Long = 0,
    @Column("target_node_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var targetNodeId: Long = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
