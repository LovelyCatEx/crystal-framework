/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.audit.controller.manager.session.dto

import com.lovelycatv.crystalframework.shared.request.PageQuery

data class SessionSearchDTO(
    override val page: Int,
    override val pageSize: Int,
    val sessionId: String?,
    val type: Int?,
) : PageQuery(page, pageSize)
