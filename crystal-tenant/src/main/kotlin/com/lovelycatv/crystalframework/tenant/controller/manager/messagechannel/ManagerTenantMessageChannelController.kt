package com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.controller.StandardTenantManagerController
import com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto.ManagerCreateTenantMessageChannelDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto.ManagerDeleteTenantMessageChannelDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto.ManagerReadTenantMessageChannelDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto.ManagerUpdateTenantMessageChannelDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantMessageChannelEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMessageChannelRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantMessageChannelManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/message-channel")
class ManagerTenantMessageChannelController(
    managerService: TenantMessageChannelManagerService,
) : StandardTenantManagerController<
        TenantMessageChannelManagerService,
        TenantMessageChannelRepository,
        TenantMessageChannelEntity,
        ManagerCreateTenantMessageChannelDTO,
        ManagerReadTenantMessageChannelDTO,
        ManagerUpdateTenantMessageChannelDTO,
        ManagerDeleteTenantMessageChannelDTO
>(
    managerService,
    createPermission = SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_CREATE,
    scopedCreatePermission = TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_CREATE_PEM,
    readPermission = SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_READ,
    scopedReadPermission = TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_READ_PEM,
    updatePermission = SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_UPDATE,
    scopedUpdatePermission = TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_UPDATE_PEM,
    deletePermission = SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_DELETE,
    scopedDeletePermission = TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_DELETE_PEM,
)
