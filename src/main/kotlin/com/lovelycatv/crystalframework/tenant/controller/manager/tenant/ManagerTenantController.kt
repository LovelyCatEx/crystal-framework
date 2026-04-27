package com.lovelycatv.crystalframework.tenant.controller.manager.tenant

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerCreateTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerDeleteTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerReadTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerUpdateTenantDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = [SystemPermission.ACTION_TENANT_READ],
    readAll = [SystemPermission.ACTION_TENANT_READ],
    create = [SystemPermission.ACTION_TENANT_CREATE],
    update = [SystemPermission.ACTION_TENANT_UPDATE],
    delete = [SystemPermission.ACTION_TENANT_DELETE],
)
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant")
class ManagerTenantController(
    managerService: TenantManagerService
) : StandardManagerController<
        TenantManagerService,
        TenantRepository,
        TenantEntity,
        ManagerCreateTenantDTO,
        ManagerReadTenantDTO,
        ManagerUpdateTenantDTO,
        ManagerDeleteTenantDTO
>(managerService)
