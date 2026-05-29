package com.lovelycatv.crystalframework.tenant.entity

import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(TableConstants.TABLE_TENANT_TIRE_BENEFIT_FEATURES)
class TenantTireBenefitFeatureEntity(
    id: Long = 0,
    @Column("feature_key") var featureKey: String = "",
    @Column("name") var name: String = "",
    @Column("description") var description: String? = null,
    @Column("feature_type") var featureType: Int = 0,
    @Column("default_value") var defaultValue: String? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
