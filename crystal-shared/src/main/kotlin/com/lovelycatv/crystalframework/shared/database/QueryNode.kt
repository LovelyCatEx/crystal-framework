package com.lovelycatv.crystalframework.shared.database

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * A node in a query condition tree.
 *
 * There are two concrete types:
 * - [ConditionNode]: a leaf node representing a single field comparison.
 * - [GroupNode]: an interior node that combines child nodes with [AND][QueryLogic.AND]
 *   or [OR][QueryLogic.OR] logic.
 *
 * This allows arbitrarily nested boolean expressions, for example:
 * ```
 * A AND B AND C
 * ```
 * where B itself is:
 * ```
 * D OR (E AND F)
 * ```
 *
 * JSON representation:
 * ```json
 * {
 *   "type": "group",
 *   "logic": "and",
 *   "children": [
 *     { "type": "condition", "field": "username", "operator": "contains", "value": "admin" },
 *     {
 *       "type": "group",
 *       "logic": "or",
 *       "children": [
 *         { "type": "condition", "field": "email", "operator": "eq", "value": "a@b.com" },
 *         {
 *           "type": "group",
 *           "logic": "and",
 *           "children": [
 *             { "type": "condition", "field": "nickname", "operator": "contains", "value": "dev" },
 *             { "type": "condition", "field": "created_time", "operator": "gte", "value": 1700000000000 }
 *           ]
 *         }
 *       ]
 *     }
 *   ]
 * }
 * ```
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ConditionNode::class, name = "condition"),
    JsonSubTypes.Type(value = GroupNode::class, name = "group"),
)
sealed class QueryNode
