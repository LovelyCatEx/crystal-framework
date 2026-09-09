package com.lovelycatv.crystalframework.ai.controller.manager.model

import com.lovelycatv.crystalframework.ai.constants.AiPermission
import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerCreateAiModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerDeleteAiModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerReadAiModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerUpdateAiModelDTO
import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.repository.AiModelRepository
import com.lovelycatv.crystalframework.ai.service.manager.AiModelManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/ai-model")
class AiModelManagerController(
    managerService: AiModelManagerService,
) : StandardManagerController<
    AiModelManagerService,
    AiModelRepository,
    AiModelEntity,
    ManagerCreateAiModelDTO,
    ManagerReadAiModelDTO,
    ManagerUpdateAiModelDTO,
    ManagerDeleteAiModelDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = AiPermission.ACTION_SYSTEM_AI_MODEL_CREATE.name,
        systemRead = AiPermission.ACTION_SYSTEM_AI_MODEL_READ.name,
        systemUpdate = AiPermission.ACTION_SYSTEM_AI_MODEL_UPDATE.name,
        systemDelete = AiPermission.ACTION_SYSTEM_AI_MODEL_DELETE.name,
    ),
)
