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
