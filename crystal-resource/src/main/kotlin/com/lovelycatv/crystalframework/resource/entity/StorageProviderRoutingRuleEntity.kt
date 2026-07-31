package com.lovelycatv.crystalframework.resource.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.resource.types.RuleDistributionType
import com.lovelycatv.crystalframework.shared.database.GroupNode
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.utils.parseObject
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("storage_provider_routing_rules")
class StorageProviderRoutingRuleEntity(
    id: Long = 0,
    @Column(value = "name")
    var name: String = "",
    @Column(value = "priority")
    var priority: Int = 0,
    @Column(value = "condition_tree")
    var conditionTree: String? = null,
    @Column(value = "target_provider_ids")
    var targetProviderIds: String = "[]",
    @Column(value = "distribution_type")
    var distributionType: Int = 0,
    @Column(value = "enabled")
    var enabled: Boolean = true,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getRealDistributionType(): RuleDistributionType {
        return RuleDistributionType.getByTypeId(this.distributionType)
            ?: throw BusinessException("rule distribution type ${this.distributionType} not found")
    }

    @JsonIgnore
    fun parseConditionTree(): GroupNode? {
        return this.conditionTree?.parseObject<GroupNode>()
    }

    @JsonIgnore
    fun parseTargetProviderIds(): List<Long> {
        return this.targetProviderIds.parseObject<List<*>>().map { it.toString().toLong() }
    }
}
