package com.lovelycatv.crystalframework.approval.entity

import com.lovelycatv.crystalframework.approval.types.ApprovalFlowScope
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowTaskStatus
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("approval_flow_task")
class ApprovalFlowTaskEntity(
    id: Long = 0,
    @Column("scope")
    var scope: Int = ApprovalFlowScope.TENANT.typeId,
    @Column("scope_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var scopeId: Long = 0,
    @Column("instance_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var instanceId: Long = 0,
    @Column("node_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var nodeId: Long = 0,
    @Column("token_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var tokenId: Long = 0,
    @Column("assignee_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var assigneeId: Long = 0,
    @Column("status")
    var status: Int = ApprovalFlowTaskStatus.PENDING.typeId,
    @Column("comment")
    var comment: String? = null,
    @Column("form_data")
    var formData: String? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {

    fun getRealStatus(): ApprovalFlowTaskStatus? = ApprovalFlowTaskStatus.getById(status)
}
