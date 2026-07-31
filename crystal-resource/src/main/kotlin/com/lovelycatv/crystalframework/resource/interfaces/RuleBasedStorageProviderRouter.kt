package com.lovelycatv.crystalframework.resource.interfaces

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.StorageProviderRoutingRuleService
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import com.lovelycatv.crystalframework.resource.service.impl.StorageProviderRoutingRuleServiceImpl
import com.lovelycatv.crystalframework.resource.types.RuleDistributionType
import com.lovelycatv.crystalframework.resource.types.RuleEvaluationTrace
import com.lovelycatv.crystalframework.resource.types.SimulationResult
import com.lovelycatv.crystalframework.shared.database.QueryNodeEvaluator
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import kotlinx.coroutines.runBlocking

/**
 * Matches [StorageProviderRoutingRuleEntity] rows top-down by priority against the upload's
 * [RoutingContext], routing to the first matching rule's target providers. A rule with a null
 * `conditionTree` matches everything (fallback rule). Throws when no rule matches at all —
 * v1 deliberately does not fall back to [RandomStorageProviderRouter] (see plan): an admin who
 * enables the rule engine is expected to always ship a catch-all rule.
 */
class RuleBasedStorageProviderRouter(
    private val storageProviderRoutingRuleService: StorageProviderRoutingRuleService,
    private val storageProviderService: StorageProviderService,
) : StorageProviderRouter {
    override suspend fun get(context: RoutingContext): StorageProviderEntity {
        val rules = storageProviderRoutingRuleService.getActiveRulesSortedByPriority()
        val evaluationMap = context.toEvaluationMap()

        for (rule in rules) {
            val tree = rule.parseConditionTree()
            if (tree != null && !QueryNodeEvaluator.evaluate(tree, evaluationMap)) {
                continue
            }

            val candidates = rule.parseTargetProviderIds()
                .mapNotNull { storageProviderService.getByIdOrNull(it) }
                .filter { it.active }

            if (candidates.isNotEmpty()) {
                return pickByDistribution(candidates, rule.getRealDistributionType())
            }
        }

        throw BusinessException("no storage provider routing rule matched for context: $context")
    }

    /**
     * Dry-run variant of [get] used by the simulate endpoint. Traverses **all** active rules (not
     * just up to the first match) to collect a full [RuleEvaluationTrace] per rule, then picks
     * `finalProvider` using the same distribution logic as [get] but only from the first matched
     * rule. Never throws — returns `noRuleMatched = true` when no rule matches so admins can see
     * that an actual upload with this context would fail.
     */
    suspend fun simulate(context: RoutingContext): SimulationResult {
        val rules = storageProviderRoutingRuleService.getActiveRulesSortedByPriority()
        val evaluationMap = context.toEvaluationMap()

        var finalProvider: StorageProviderEntity? = null
        val traces = rules.map { rule ->
            val tree = rule.parseConditionTree()
            val conditionTrace = tree?.let { QueryNodeEvaluator.evaluateWithTrace(it, evaluationMap) }
            val matched = conditionTrace?.matched ?: true
            val providerIds = if (matched) rule.parseTargetProviderIds() else null

            if (matched && finalProvider == null) {
                val candidates = rule.parseTargetProviderIds()
                    .mapNotNull { storageProviderService.getByIdOrNull(it) }
                    .filter { it.active }
                if (candidates.isNotEmpty()) {
                    finalProvider = pickByDistribution(candidates, rule.getRealDistributionType())
                }
            }

            RuleEvaluationTrace(
                rule = rule,
                matched = matched,
                conditionTrace = conditionTrace,
                selectedProviderIds = providerIds,
            )
        }

        return SimulationResult(
            ruleTraces = traces,
            finalProvider = finalProvider,
            noRuleMatched = traces.none { it.matched },
        )
    }

    private fun pickByDistribution(
        candidates: List<StorageProviderEntity>,
        distributionType: RuleDistributionType
    ): StorageProviderEntity {
        return when (distributionType) {
            RuleDistributionType.FIRST_AVAILABLE -> candidates.first()
            RuleDistributionType.RANDOM -> candidates.random()
        }
    }

    /**
     * Invoked from a non-suspend, @Async event listener, so the reactive cache removal is
     * bridged with [runBlocking] (runs off the reactor event-loop), mirroring
     * [RandomStorageProviderRouter.invalidateCache].
     */
    override fun invalidateCache() = runBlocking {
        storageProviderRoutingRuleService.removeListCache(StorageProviderRoutingRuleServiceImpl.CACHE_KEY_IDENTIFIER)
    }
}
