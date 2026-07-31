package com.lovelycatv.crystalframework.resource.controller.manager.routing.vo

data class SimulationResultVO(
    val ruleTraces: List<RuleEvaluationTraceVO>,
    val finalProvider: StorageProviderSimpleVO?,
    val noRuleMatched: Boolean,
)
