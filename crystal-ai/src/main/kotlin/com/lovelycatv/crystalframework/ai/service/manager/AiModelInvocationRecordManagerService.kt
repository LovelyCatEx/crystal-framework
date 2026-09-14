package com.lovelycatv.crystalframework.ai.service.manager

import com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto.ManagerCreateAiModelInvocationRecordDTO
import com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto.ManagerDeleteAiModelInvocationRecordDTO
import com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto.ManagerReadAiModelInvocationRecordDTO
import com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto.ManagerUpdateAiModelInvocationRecordDTO
import com.lovelycatv.crystalframework.ai.entity.AiModelInvocationRecordEntity
import com.lovelycatv.crystalframework.ai.repository.AiModelInvocationRecordRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface AiModelInvocationRecordManagerService : CachedBaseManagerService<
    AiModelInvocationRecordRepository,
    AiModelInvocationRecordEntity,
    ManagerCreateAiModelInvocationRecordDTO,
    ManagerReadAiModelInvocationRecordDTO,
    ManagerUpdateAiModelInvocationRecordDTO,
    ManagerDeleteAiModelInvocationRecordDTO
>
