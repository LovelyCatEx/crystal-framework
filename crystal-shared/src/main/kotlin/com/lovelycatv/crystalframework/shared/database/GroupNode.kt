package com.lovelycatv.crystalframework.shared.database

/**
 * An interior node in a [QueryNode] tree that combines its [children] with
 * [AND][QueryLogic.AND] or [OR][QueryLogic.OR] logic.
 *
 * Children can be either [ConditionNode]s or nested [GroupNode]s, allowing
 * arbitrarily deep boolean expressions.
 *
 * @param logic    How to combine the children: [QueryLogic.AND] or [QueryLogic.OR].
 * @param children The child nodes. Must be non-empty.
 */
data class GroupNode(
    val logic: QueryLogic,
    val children: List<QueryNode>,
) : QueryNode()
