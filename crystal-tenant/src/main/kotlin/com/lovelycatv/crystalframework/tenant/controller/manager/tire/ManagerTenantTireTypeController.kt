package com.lovelycatv.crystalframework.tenant.controller.manager.tire

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerCreateTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerDeleteTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerReadTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerUpdateTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantTireTypeEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireTypeRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireTypeManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/tire")
class ManagerTenantTireTypeController(
    managerService: TenantTireTypeManagerService
) : StandardManagerController<
        TenantTireTypeManagerService,
        TenantTireTypeRepository,
        TenantTireTypeEntity,
        ManagerCreateTenantTireTypeDTO,
        ManagerReadTenantTireTypeDTO,
        ManagerUpdateTenantTireTypeDTO,
        ManagerDeleteTenantTireTypeDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = PermissionMatrix.NOT_APPLICABLE,
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        systemUpdate = PermissionMatrix.NOT_APPLICABLE,
        systemDelete = PermissionMatrix.NOT_APPLICABLE,
        superCreate = SystemPermission.ACTION_TENANT_TIRE_TYPE_CREATE,
        superRead = SystemPermission.ACTION_TENANT_TIRE_TYPE_READ,
        superUpdate = SystemPermission.ACTION_TENANT_TIRE_TYPE_UPDATE,
        superDelete = SystemPermission.ACTION_TENANT_TIRE_TYPE_DELETE,
    ),
)
