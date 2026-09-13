/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.database

/**
 * Collect every `ConditionNode.field` referenced anywhere in this query tree.
 * Used to validate that all referenced fields are on the entity's queryable allowlist
 * before criteria construction.
 */
fun QueryNode.collectFields(): Set<String> {
    val out = mutableSetOf<String>()
    collectFieldsInto(this, out)
    return out
}

private fun collectFieldsInto(node: QueryNode, sink: MutableSet<String>) {
    when (node) {
        is ConditionNode -> sink.add(node.field)
        is GroupNode -> node.children.forEach { collectFieldsInto(it, sink) }
    }
}
