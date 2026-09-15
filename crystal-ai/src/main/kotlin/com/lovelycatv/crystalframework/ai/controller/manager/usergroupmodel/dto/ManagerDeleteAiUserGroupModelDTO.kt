/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteAiUserGroupModelDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
