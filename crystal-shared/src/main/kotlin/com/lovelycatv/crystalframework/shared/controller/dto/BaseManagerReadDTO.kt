/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.controller.dto

import com.lovelycatv.crystalframework.shared.database.QueryNode
import com.lovelycatv.crystalframework.shared.request.PageQuery

open class BaseManagerReadDTO(
    override val page: Int,
    override val pageSize: Int,
    open val id: Long? = null,
    /**
     * Root node of a structured query condition tree.
     *
     * All filtering is expressed through this tree. The query engine uses
     * [R2dbcEntityTemplate] with the criteria built from this tree.
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
