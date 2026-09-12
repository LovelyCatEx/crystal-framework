package com.lovelycatv.crystalframework.ai.service.manager

import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerCreateAiProviderDTO
import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerDeleteAiProviderDTO
import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerReadAiProviderDTO
import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerUpdateAiProviderDTO
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.repository.AiProviderRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface AiProviderManagerService : CachedBaseManagerService<
    AiProviderRepository,
    AiProviderEntity,
    ManagerCreateAiProviderDTO,
    ManagerReadAiProviderDTO,
    ManagerUpdateAiProviderDTO,
    ManagerDeleteAiProviderDTO
>
