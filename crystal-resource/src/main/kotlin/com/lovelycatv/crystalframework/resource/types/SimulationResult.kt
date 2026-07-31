package com.lovelycatv.crystalframework.resource.types

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity

/**
 * Full result of a routing simulation: every active rule's evaluation trace, the final selected
 * provider (from the first matched rule after distribution-type selection), and a fallback flag.
 * Unlike `RuleBasedStorageProviderRouter.get` which throws on no-match, `simulate` returns
 * [noRuleMatched] = true to let callers surface the "no rule matched — upload would fail" state.
 */
data class SimulationResult(
    val ruleTraces: List<RuleEvaluationTrace>,
    val finalProvider: StorageProviderEntity?,
    val noRuleMatched: Boolean,
)
