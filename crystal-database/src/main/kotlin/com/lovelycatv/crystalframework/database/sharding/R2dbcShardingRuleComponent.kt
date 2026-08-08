package com.lovelycatv.crystalframework.database.sharding

interface R2dbcShardingRuleComponent {
    val name: String
    fun rules(): Collection<R2dbcShardingRule>
}
