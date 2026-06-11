package com.lovelycatv.crystalframework.approval.entity

import com.lovelycatv.crystalframework.approval.types.ApprovalFlowDefinitionStatus
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowScope
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("approval_flow_definition")
class ApprovalFlowDefinitionEntity(
    id: Long = 0,
    @Column("scope")
    var scope: Int = ApprovalFlowScope.TENANT.typeId,
    @Column("scope_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var scopeId: Long = 0,
    @Column("name")
    var name: String = "",
    @Column("description")
    var description: String? = null,
    @Column("current_version")
    var currentVersion: Int = 1,
    @Column("status")
    var status: Int = ApprovalFlowDefinitionStatus.DRAFT.typeId,
    @Column("form_schema")
    var formSchema: String? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {

    fun getRealScope(): ApprovalFlowScope? = ApprovalFlowScope.getById(scope)

    fun getRealStatus(): ApprovalFlowDefinitionStatus? = ApprovalFlowDefinitionStatus.getById(status)
}
