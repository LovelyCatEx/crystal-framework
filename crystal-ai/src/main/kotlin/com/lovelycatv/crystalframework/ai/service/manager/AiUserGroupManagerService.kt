package com.lovelycatv.crystalframework.ai.service.manager

import com.lovelycatv.crystalframework.ai.controller.manager.usergroup.dto.ManagerCreateAiUserGroupDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroup.dto.ManagerDeleteAiUserGroupDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroup.dto.ManagerReadAiUserGroupDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroup.dto.ManagerUpdateAiUserGroupDTO
import com.lovelycatv.crystalframework.ai.entity.AiUserGroupEntity
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface AiUserGroupManagerService : CachedBaseManagerService<
    AiUserGroupRepository,
    AiUserGroupEntity,
    ManagerCreateAiUserGroupDTO,
    ManagerReadAiUserGroupDTO,
    ManagerUpdateAiUserGroupDTO,
    ManagerDeleteAiUserGroupDTO
>
