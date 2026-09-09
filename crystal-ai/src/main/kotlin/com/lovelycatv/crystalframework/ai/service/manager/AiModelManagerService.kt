package com.lovelycatv.crystalframework.ai.service.manager

import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerCreateAiModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerDeleteAiModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerReadAiModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerUpdateAiModelDTO
import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.repository.AiModelRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface AiModelManagerService : CachedBaseManagerService<
    AiModelRepository,
    AiModelEntity,
    ManagerCreateAiModelDTO,
    ManagerReadAiModelDTO,
    ManagerUpdateAiModelDTO,
    ManagerDeleteAiModelDTO
>
