package com.lovelycatv.crystalframework.tenant.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.entity.ScopedEntity
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.tenant.types.DepartmentMemberRoleType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("tenant_department_member_relations")
class TenantDepartmentMemberRelationEntity(
    id: Long = 0,
    @Column(value = "department_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var departmentId: Long = 0,
    @Column(value = "member_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var memberId: Long = 0,
    @Column(value = "role_type")
    var roleType: Int = DepartmentMemberRoleType.MEMBER.typeId,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime), ScopedEntity<Long> {
    override fun getDirectParentId(): Long = this.departmentId

    @JsonIgnore
    fun getRealRoleType(): DepartmentMemberRoleType {
        return DepartmentMemberRoleType.getById(this.roleType)
            ?: throw BusinessException("invalid department member role type id $roleType")
    }
}
