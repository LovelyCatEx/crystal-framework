package com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel

import com.lovelycatv.crystalframework.ai.constants.AiPermission
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto.ManagerCreateAiUserGroupModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto.ManagerDeleteAiUserGroupModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto.ManagerReadAiUserGroupModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto.ManagerUpdateAiUserGroupModelDTO
import com.lovelycatv.crystalframework.ai.entity.AiUserGroupModelEntity
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupModelRepository
import com.lovelycatv.crystalframework.ai.service.manager.AiUserGroupModelManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/ai-user-group-model")
class AiUserGroupModelManagerController(
    managerService: AiUserGroupModelManagerService
) : StandardManagerController<
    AiUserGroupModelManagerService,
    AiUserGroupModelRepository,
    AiUserGroupModelEntity,
    ManagerCreateAiUserGroupModelDTO,
    ManagerReadAiUserGroupModelDTO,
    ManagerUpdateAiUserGroupModelDTO,
    ManagerDeleteAiUserGroupModelDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = AiPermission.ACTION_SYSTEM_AI_USER_GROUP_MODEL_CREATE_NAME,
        systemRead = AiPermission.ACTION_SYSTEM_AI_USER_GROUP_MODEL_READ_NAME,
        systemUpdate = PermissionMatrix.NEVER_GRANTED,  // Update is not allowed for user group model associations
        systemDelete = AiPermission.ACTION_SYSTEM_AI_USER_GROUP_MODEL_DELETE_NAME
    )
)
