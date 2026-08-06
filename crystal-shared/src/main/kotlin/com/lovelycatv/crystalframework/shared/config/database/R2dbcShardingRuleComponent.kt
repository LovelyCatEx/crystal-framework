package com.lovelycatv.crystalframework.shared.config.database

interface R2dbcShardingRuleComponent {
    val name: String
    fun rules(): Collection<R2dbcShardingRule>
}
