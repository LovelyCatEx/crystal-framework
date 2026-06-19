package com.lovelycatv.crystalframework.approval.entity

import com.lovelycatv.crystalframework.approval.types.ApprovalFlowNodeType
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("approval_flow_node")
class ApprovalFlowNodeEntity(
    id: Long = 0,
    @Column("definition_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var definitionId: Long = 0,
    @Column("definition_version")
    var definitionVersion: Int = 0,
    @Column("node_key")
    var nodeKey: String = "",
    @Column("type")
    var type: Int = ApprovalFlowNodeType.START.typeId,
    @Column("name")
    var name: String = "",
    @Column("config")
    var config: String? = null,
    @Column("form_schema")
    var formSchema: String? = null,
    @Column("position_x")
    var positionX: Int = 0,
    @Column("position_y")
    var positionY: Int = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {

    fun getRealType(): ApprovalFlowNodeType? = ApprovalFlowNodeType.getById(type)
}
