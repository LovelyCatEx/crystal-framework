/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.service.manager

import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto.ManagerCreateAiUserGroupModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto.ManagerDeleteAiUserGroupModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto.ManagerReadAiUserGroupModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto.ManagerUpdateAiUserGroupModelDTO
import com.lovelycatv.crystalframework.ai.entity.AiUserGroupModelEntity
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupModelRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface AiUserGroupModelManagerService : CachedBaseManagerService<
    AiUserGroupModelRepository,
    AiUserGroupModelEntity,
    ManagerCreateAiUserGroupModelDTO,
    ManagerReadAiUserGroupModelDTO,
    ManagerUpdateAiUserGroupModelDTO,
    ManagerDeleteAiUserGroupModelDTO
> {
    override fun getRepository(): AiUserGroupModelRepository
}
