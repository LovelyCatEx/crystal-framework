package com.lovelycatv.crystalframework.shared.controller.dto

import com.lovelycatv.crystalframework.shared.database.QueryNode
import com.lovelycatv.crystalframework.shared.request.PageQuery

open class BaseManagerReadDTO(
    override val page: Int,
    override val pageSize: Int,
    open val id: Long? = null,
    open val searchKeyword: String? = null,
    open val startTime: Long? = null,
    open val endTime: Long? = null,
    /**
     * Root node of a structured query condition tree.
     *
     * When provided, the query engine uses [R2dbcEntityTemplate] with the
     * criteria built from this tree, bypassing all legacy SQL paths.
     *
     * The tree supports arbitrary AND/OR nesting via [GroupNode] and leaf
     * predicates via [ConditionNode]. Field names must match database column names.
     *
     * Example JSON:
     * ```json
     * {
     *   "type": "group", "logic": "and",
     *   "children": [
     *     { "type": "condition", "field": "username", "operator": "contains", "value": "admin" },
     *     {
     *       "type": "group", "logic": "or",
     *       "children": [
     *         { "type": "condition", "field": "email", "operator": "eq", "value": "a@b.com" },
     *         { "type": "condition", "field": "created_time", "operator": "gte", "value": 1700000000000 }
     *       ]
     *     }
     *   ]
     * }
     * ```
     */
    open val query: QueryNode? = null,
) : PageQuery(page, pageSize)
