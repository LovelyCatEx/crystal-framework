package com.lovelycatv.crystalframework.approval.entity

import com.lovelycatv.crystalframework.approval.types.ApprovalFlowDefinitionStatus
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowScope
import com.lovelycatv.crystalframework.shared.types.entity.BaseScopedEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("approval_flow_definition")
class ApprovalFlowDefinitionEntity(
    id: Long = 0,
    scope: Int = ApprovalFlowScope.TENANT.typeId,
    scopeId: Long = 0,
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
) : BaseScopedEntity(id, scope, scopeId, createdTime, modifiedTime, deletedTime) {

    fun getRealScope(): ApprovalFlowScope? = ApprovalFlowScope.getById(scope)

    fun getRealStatus(): ApprovalFlowDefinitionStatus? = ApprovalFlowDefinitionStatus.getById(status)
}
