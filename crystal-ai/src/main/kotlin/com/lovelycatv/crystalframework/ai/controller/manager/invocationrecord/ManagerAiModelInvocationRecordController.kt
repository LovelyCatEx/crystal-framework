package com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord

import com.lovelycatv.crystalframework.ai.constants.AiPermission
import com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto.ManagerCreateAiModelInvocationRecordDTO
import com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto.ManagerDeleteAiModelInvocationRecordDTO
import com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto.ManagerReadAiModelInvocationRecordDTO
import com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto.ManagerUpdateAiModelInvocationRecordDTO
import com.lovelycatv.crystalframework.ai.entity.AiModelInvocationRecordEntity
import com.lovelycatv.crystalframework.ai.repository.AiModelInvocationRecordRepository
import com.lovelycatv.crystalframework.ai.service.manager.AiModelInvocationRecordManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.ReadonlyManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnlyReadonly
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/ai-model-invocation-record")
class ManagerAiModelInvocationRecordController(
    managerService: AiModelInvocationRecordManagerService
) : ReadonlyManagerController<
    AiModelInvocationRecordManagerService,
    AiModelInvocationRecordRepository,
    AiModelInvocationRecordEntity,
    ManagerCreateAiModelInvocationRecordDTO,
    ManagerReadAiModelInvocationRecordDTO,
    ManagerUpdateAiModelInvocationRecordDTO,
    ManagerDeleteAiModelInvocationRecordDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnlyReadonly(
        systemRead = AiPermission.ACTION_SYSTEM_AI_INVOCATION_RECORD_READ_NAME,
    ),
)
