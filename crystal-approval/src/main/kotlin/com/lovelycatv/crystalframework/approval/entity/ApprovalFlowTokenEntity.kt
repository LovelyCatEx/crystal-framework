package com.lovelycatv.crystalframework.approval.entity

import com.lovelycatv.crystalframework.approval.types.ApprovalFlowTokenStatus
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("approval_flow_token")
class ApprovalFlowTokenEntity(
    id: Long = 0,
    @Column("instance_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var instanceId: Long = 0,
    @Column("current_node_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var currentNodeId: Long = 0,
    @Column("status")
    var status: Int = ApprovalFlowTokenStatus.ACTIVE.typeId,
    @Column("fork_node_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var forkNodeId: Long? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {

    fun getRealStatus(): ApprovalFlowTokenStatus? = ApprovalFlowTokenStatus.getById(status)
}
