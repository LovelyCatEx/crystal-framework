package com.lovelycatv.crystalframework.tenant.controller.manager.benefit

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerDeleteTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerReadTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerUpdateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantTireBenefitValueEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitValueRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitValueManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/tire/benefit/value")
class ManagerTenantTireBenefitValueController(
    managerService: TenantTireBenefitValueManagerService
) : StandardManagerController<
        TenantTireBenefitValueManagerService,
        TenantTireBenefitValueRepository,
        TenantTireBenefitValueEntity,
        ManagerCreateTenantTireBenefitValueDTO,
        ManagerReadTenantTireBenefitValueDTO,
        ManagerUpdateTenantTireBenefitValueDTO,
        ManagerDeleteTenantTireBenefitValueDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_TENANT_TIRE_BENEFIT_VALUE_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_TENANT_TIRE_BENEFIT_VALUE_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_TENANT_TIRE_BENEFIT_VALUE_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_TENANT_TIRE_BENEFIT_VALUE_DELETE.name,
    ),
)
