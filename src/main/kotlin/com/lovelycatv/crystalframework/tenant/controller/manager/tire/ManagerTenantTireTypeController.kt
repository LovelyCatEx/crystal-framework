package com.lovelycatv.crystalframework.tenant.controller.manager.tire

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
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

@ManagerPermissions(
    read = [SystemPermission.ACTION_TENANT_TIRE_TYPE_READ],
    readAll = [SystemPermission.ACTION_TENANT_TIRE_TYPE_READ],
    create = [SystemPermission.ACTION_TENANT_TIRE_TYPE_CREATE],
    update = [SystemPermission.ACTION_TENANT_TIRE_TYPE_UPDATE],
    delete = [SystemPermission.ACTION_TENANT_TIRE_TYPE_DELETE],
)
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
>(managerService)
