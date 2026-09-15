/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadAiUserGroupMemberDTO(
    override val page: Int = 1,
    override val pageSize: Int = 20,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val userGroupId: Long? = null,
    val userId: Long? = null
) : BaseManagerReadDTO(page, pageSize)
