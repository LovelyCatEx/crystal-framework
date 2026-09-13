/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateAiUserGroupMemberDTO(
    override val id: Long,
    val userGroupId: Long? = null,
    val userId: Long? = null
) : BaseManagerUpdateDTO(id)
