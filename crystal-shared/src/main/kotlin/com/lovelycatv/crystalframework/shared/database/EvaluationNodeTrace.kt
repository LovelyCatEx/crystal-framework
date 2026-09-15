/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.database

/**
 * Result of [QueryNodeEvaluator.evaluateWithTrace]: a tree mirroring the original [QueryNode]
 * with per-node evaluation outcome annotated. Used by simulation / dry-run features that need to
 * explain why a rule matched or didn't (e.g. storage provider routing rule simulate endpoint).
 *
 * Two concrete types:
 * - [EvaluationLeafTrace]: annotated result of a [ConditionNode]
 * - [EvaluationGroupTrace]: annotated result of a [GroupNode]
 */
sealed class EvaluationNodeTrace {
    abstract val matched: Boolean
}
