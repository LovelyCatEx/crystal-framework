package com.lovelycatv.crystalframework.ai.controller.manager.usergroup

import com.lovelycatv.crystalframework.ai.constants.AiPermission
import com.lovelycatv.crystalframework.ai.controller.manager.usergroup.dto.ManagerCreateAiUserGroupDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroup.dto.ManagerDeleteAiUserGroupDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroup.dto.ManagerReadAiUserGroupDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroup.dto.ManagerUpdateAiUserGroupDTO
import com.lovelycatv.crystalframework.ai.entity.AiUserGroupEntity
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupRepository
import com.lovelycatv.crystalframework.ai.service.manager.AiUserGroupManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/ai-user-group")
class AiUserGroupManagerController(
    managerService: AiUserGroupManagerService,
) : StandardManagerController<
    AiUserGroupManagerService,
    AiUserGroupRepository,
    AiUserGroupEntity,
    ManagerCreateAiUserGroupDTO,
    ManagerReadAiUserGroupDTO,
    ManagerUpdateAiUserGroupDTO,
    ManagerDeleteAiUserGroupDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = AiPermission.ACTION_SYSTEM_AI_USER_GROUP_CREATE.name,
        systemRead = AiPermission.ACTION_SYSTEM_AI_USER_GROUP_READ.name,
        systemUpdate = AiPermission.ACTION_SYSTEM_AI_USER_GROUP_UPDATE.name,
        systemDelete = AiPermission.ACTION_SYSTEM_AI_USER_GROUP_DELETE.name,
    ),
)
