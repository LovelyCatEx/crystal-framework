/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateAiUserGroupModelDTO(
    override val id: Long,
    val userGroupId: Long? = null,
    val modelId: Long? = null
) : BaseManagerUpdateDTO(id)
