package com.lovelycatv.crystalframework.ai.service.manager

import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto.ManagerCreateAiUserGroupMemberDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto.ManagerDeleteAiUserGroupMemberDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto.ManagerReadAiUserGroupMemberDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmember.dto.ManagerUpdateAiUserGroupMemberDTO
import com.lovelycatv.crystalframework.ai.entity.AiUserGroupMemberEntity
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupMemberRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface AiUserGroupMemberManagerService : CachedBaseManagerService<
    AiUserGroupMemberRepository,
    AiUserGroupMemberEntity,
    ManagerCreateAiUserGroupMemberDTO,
    ManagerReadAiUserGroupMemberDTO,
    ManagerUpdateAiUserGroupMemberDTO,
    ManagerDeleteAiUserGroupMemberDTO
> {
    override fun getRepository(): AiUserGroupMemberRepository
}
