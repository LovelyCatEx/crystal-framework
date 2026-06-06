package com.lovelycatv.crystalframework.tenant.entity

import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(TableConstants.TABLE_TENANT_TIRE_BENEFIT_VALUES)
class TenantTireBenefitValueEntity(
    id: Long = 0,
    @Column("tire_type_id") var tireTypeId: Long = 0,
    @Column("feature_id") var featureId: Long = 0,
    @Column("feature_value") var featureValue: String = "",
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
