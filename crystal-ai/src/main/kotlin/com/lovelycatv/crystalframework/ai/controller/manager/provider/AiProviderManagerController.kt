package com.lovelycatv.crystalframework.ai.controller.manager.provider

import com.lovelycatv.crystalframework.ai.constants.AiPermission
import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerCreateAiProviderDTO
import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerDeleteAiProviderDTO
import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerReadAiProviderDTO
import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerUpdateAiProviderDTO
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.repository.AiProviderRepository
import com.lovelycatv.crystalframework.ai.service.manager.AiProviderManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/ai-provider")
class AiProviderManagerController(
    managerService: AiProviderManagerService,
) : StandardManagerController<
    AiProviderManagerService,
    AiProviderRepository,
    AiProviderEntity,
    ManagerCreateAiProviderDTO,
    ManagerReadAiProviderDTO,
    ManagerUpdateAiProviderDTO,
    ManagerDeleteAiProviderDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = AiPermission.ACTION_SYSTEM_AI_PROVIDER_CREATE.name,
        systemRead = AiPermission.ACTION_SYSTEM_AI_PROVIDER_READ.name,
        systemUpdate = AiPermission.ACTION_SYSTEM_AI_PROVIDER_UPDATE.name,
        systemDelete = AiPermission.ACTION_SYSTEM_AI_PROVIDER_DELETE.name,
    ),
)
