package com.lovelycatv.crystalframework.approval.entity

import com.lovelycatv.crystalframework.approval.types.ApprovalFlowInstanceStatus
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowScope
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("approval_flow_instance")
class ApprovalFlowInstanceEntity(
    id: Long = 0,
    @Column("scope")
    var scope: Int = ApprovalFlowScope.TENANT.typeId,
    @Column("scope_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var scopeId: Long = 0,
    @Column("definition_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var definitionId: Long = 0,
    @Column("definition_version")
    var definitionVersion: Int = 0,
    @Column("initiator_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var initiatorId: Long = 0,
    @Column("status")
    var status: Int = ApprovalFlowInstanceStatus.IN_PROGRESS.typeId,
    @Column("form_data")
    var formData: String? = null,
    @Column("latest_node_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var latestNodeId: Long = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {

    fun getRealScope(): ApprovalFlowScope? = ApprovalFlowScope.getById(scope)

    fun getRealStatus(): ApprovalFlowInstanceStatus? = ApprovalFlowInstanceStatus.getById(status)
}
