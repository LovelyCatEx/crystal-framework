/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.resource.types

import com.lovelycatv.crystalframework.resource.entity.StorageProviderRoutingRuleEntity
import com.lovelycatv.crystalframework.shared.database.EvaluationNodeTrace

/**
 * Per-rule evaluation trace collected by `RuleBasedStorageProviderRouter.simulate`. A rule with a
 * null [conditionTrace] means the rule has no condition tree (matches everything — fallback rule).
 * [selectedProviderIds] is populated only when [matched] is true; the provider actually chosen for
 * the final result comes from the first matched rule in the list (see [SimulationResult]).
 */
data class RuleEvaluationTrace(
    val rule: StorageProviderRoutingRuleEntity,
    val matched: Boolean,
    val conditionTrace: EvaluationNodeTrace?,
    val selectedProviderIds: List<Long>?,
)
