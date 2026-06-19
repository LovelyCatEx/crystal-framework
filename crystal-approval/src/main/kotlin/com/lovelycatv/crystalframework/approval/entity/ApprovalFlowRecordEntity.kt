package com.lovelycatv.crystalframework.approval.entity

import com.lovelycatv.crystalframework.approval.types.ApprovalFlowRecordAction
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowScope
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("approval_flow_record")
class ApprovalFlowRecordEntity(
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
    @Column("operator_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var operatorId: Long = 0,
    @Column("action")
    var action: Int = ApprovalFlowRecordAction.INITIATE.typeId,
    @Column("comment")
    var comment: String? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {

    fun getRealAction(): ApprovalFlowRecordAction? = ApprovalFlowRecordAction.getById(action)
}
