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
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
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
    permissions = PermissionMatrix(
        superCreate = SystemPermission.ACTION_X_MESSAGE_CHANNEL_CREATE.name,
        superRead = SystemPermission.ACTION_X_MESSAGE_CHANNEL_READ.name,
        superUpdate = SystemPermission.ACTION_X_MESSAGE_CHANNEL_UPDATE.name,
        superDelete = SystemPermission.ACTION_X_MESSAGE_CHANNEL_DELETE.name,
        systemCreate = SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_DELETE.name,
        tenantAdminCreate = SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_CREATE.name,
        tenantAdminRead = SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_READ.name,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_UPDATE.name,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_DELETE.name,
        tenantPemCreate = TenantPermission.ACTION_MESSAGE_CHANNEL_CREATE.name,
        tenantPemRead = TenantPermission.ACTION_MESSAGE_CHANNEL_READ.name,
        tenantPemUpdate = TenantPermission.ACTION_MESSAGE_CHANNEL_UPDATE.name,
        tenantPemDelete = TenantPermission.ACTION_MESSAGE_CHANNEL_DELETE.name,
    ),
)
