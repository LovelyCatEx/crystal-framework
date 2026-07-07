package com.lovelycatv.crystalframework.messagechannel.controller.manager

import com.lovelycatv.crystalframework.messagechannel.controller.manager.dto.ManagerCreateMessageChannelDTO
import com.lovelycatv.crystalframework.messagechannel.controller.manager.dto.ManagerDeleteMessageChannelDTO
import com.lovelycatv.crystalframework.messagechannel.controller.manager.dto.ManagerReadMessageChannelDTO
import com.lovelycatv.crystalframework.messagechannel.controller.manager.dto.ManagerUpdateMessageChannelDTO
import com.lovelycatv.crystalframework.messagechannel.entity.MessageChannelEntity
import com.lovelycatv.crystalframework.messagechannel.repository.MessageChannelRepository
import com.lovelycatv.crystalframework.messagechannel.service.manager.MessageChannelManagerService
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.ScopedPermissionTriad
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/message-channel")
class ManagerMessageChannelController(
    managerService: MessageChannelManagerService,
) : StandardScopedManagerController<
        MessageChannelManagerService,
        MessageChannelRepository,
        MessageChannelEntity,
        ManagerCreateMessageChannelDTO,
        ManagerReadMessageChannelDTO,
        ManagerUpdateMessageChannelDTO,
        ManagerDeleteMessageChannelDTO
        >(
    managerService,
    permissions = ScopedPermissionTriad(
        superCreate = SystemPermission.ACTION_MESSAGE_CHANNEL_CREATE,
        superRead = SystemPermission.ACTION_MESSAGE_CHANNEL_READ,
        superUpdate = SystemPermission.ACTION_MESSAGE_CHANNEL_UPDATE,
        superDelete = SystemPermission.ACTION_MESSAGE_CHANNEL_DELETE,
        systemCreate = SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_CREATE,
        systemRead = SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_READ,
        systemUpdate = SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_UPDATE,
        systemDelete = SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_CREATE_PEM,
        tenantPemRead = TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_DELETE_PEM,
    ),
)
