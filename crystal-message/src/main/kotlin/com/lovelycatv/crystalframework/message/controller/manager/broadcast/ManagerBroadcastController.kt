package com.lovelycatv.crystalframework.message.controller.manager.broadcast

import com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto.ManagerCreateBroadcastDTO
import com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto.ManagerDeleteBroadcastDTO
import com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto.ManagerReadBroadcastDTO
import com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto.ManagerUpdateBroadcastDTO
import com.lovelycatv.crystalframework.message.entity.MsgBroadcastEntity
import com.lovelycatv.crystalframework.message.repository.MsgBroadcastRepository
import com.lovelycatv.crystalframework.message.service.manager.BroadcastManagerService
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Management CRUD for broadcasts (system announcements + tenant broadcasts) — one table, scope
 * discriminated. The entity carries its own `scope_type / scope_id`, so the default scoped hooks
 * apply: `scope=SYSTEM` requests authorize through super+system, `scope=TENANT` through
 * super+tenantAdmin+tenantPem, with tenantPem ownership pinned to the caller's tenantId. Sender
 * identity and scope↔audience consistency are enforced in [BroadcastManagerService].
 */
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/broadcast")
class ManagerBroadcastController(
    managerService: BroadcastManagerService,
) : StandardScopedManagerController<
        BroadcastManagerService,
        MsgBroadcastRepository,
        MsgBroadcastEntity,
        ManagerCreateBroadcastDTO,
        ManagerReadBroadcastDTO,
        ManagerUpdateBroadcastDTO,
        ManagerDeleteBroadcastDTO
        >(
    managerService,
    permissions = PermissionMatrix(
        superCreate = SystemPermission.ACTION_X_BROADCAST_CREATE.name,
        superRead = SystemPermission.ACTION_X_BROADCAST_READ.name,
        superUpdate = SystemPermission.ACTION_X_BROADCAST_UPDATE.name,
        superDelete = SystemPermission.ACTION_X_BROADCAST_DELETE.name,
        systemCreate = SystemPermission.ACTION_SYSTEM_BROADCAST_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_BROADCAST_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_BROADCAST_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_BROADCAST_DELETE.name,
        tenantAdminCreate = SystemPermission.ACTION_TENANT_BROADCAST_CREATE.name,
        tenantAdminRead = SystemPermission.ACTION_TENANT_BROADCAST_READ.name,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_BROADCAST_UPDATE.name,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_BROADCAST_DELETE.name,
        tenantPemCreate = TenantPermission.ACTION_BROADCAST_CREATE.name,
        tenantPemRead = TenantPermission.ACTION_BROADCAST_READ.name,
        tenantPemUpdate = TenantPermission.ACTION_BROADCAST_UPDATE.name,
        tenantPemDelete = TenantPermission.ACTION_BROADCAST_DELETE.name,
    ),
)
