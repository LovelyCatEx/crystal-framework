package com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember

import com.lovelycatv.crystalframework.ai.constants.AiPermission
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto.ManagerCreateAiUserGroupMemberDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto.ManagerDeleteAiUserGroupMemberDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto.ManagerReadAiUserGroupMemberDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto.ManagerUpdateAiUserGroupMemberDTO
import com.lovelycatv.crystalframework.ai.entity.AiUserGroupMemberEntity
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupMemberRepository
import com.lovelycatv.crystalframework.ai.service.manager.AiUserGroupMemberManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/ai-user-group-member")
class AiUserGroupMemberManagerController(
    managerService: AiUserGroupMemberManagerService
) : StandardManagerController<
    AiUserGroupMemberManagerService,
    AiUserGroupMemberRepository,
    AiUserGroupMemberEntity,
    ManagerCreateAiUserGroupMemberDTO,
    ManagerReadAiUserGroupMemberDTO,
    ManagerUpdateAiUserGroupMemberDTO,
    ManagerDeleteAiUserGroupMemberDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = AiPermission.ACTION_SYSTEM_AI_USER_GROUP_MEMBER_CREATE_NAME,
        systemRead = AiPermission.ACTION_SYSTEM_AI_USER_GROUP_MEMBER_READ_NAME,
        systemUpdate = PermissionMatrix.NEVER_GRANTED,  // Update is not allowed for user group member associations
        systemDelete = AiPermission.ACTION_SYSTEM_AI_USER_GROUP_MEMBER_DELETE_NAME
    )
)
