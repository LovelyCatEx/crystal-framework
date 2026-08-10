package com.lovelycatv.crystalframework.database.constants

object ShardingConstants {
    /**
     * Identity of the [com.lovelycatv.crystalframework.database.sharding.ConfigDrivenShardingRuleComponent],
     * the component contributing rules declared under `crystalframework.sharding.rules`.
     */
    const val CONFIG_DRIVEN_RULE_COMPONENT_NAME = "config-driven"

    /**
     * Name of the single SpEL variable exposed to a rule's `inline-expression`, bound to the
     * sharding-column value. Write expressions as e.g. `'users_' + (#value % 8)`.
     */
    const val INLINE_EXPRESSION_VARIABLE = "value"
}
