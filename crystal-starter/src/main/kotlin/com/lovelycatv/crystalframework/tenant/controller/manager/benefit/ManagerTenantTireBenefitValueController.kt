package com.lovelycatv.crystalframework.tenant.controller.manager.benefit

import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
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

@ManagerPermissions(
    read = [SystemPermission.ACTION_TENANT_TIRE_BENEFIT_VALUE_READ],
    readAll = [SystemPermission.ACTION_TENANT_TIRE_BENEFIT_VALUE_READ],
    create = [SystemPermission.ACTION_TENANT_TIRE_BENEFIT_VALUE_CREATE],
    update = [SystemPermission.ACTION_TENANT_TIRE_BENEFIT_VALUE_UPDATE],
    delete = [SystemPermission.ACTION_TENANT_TIRE_BENEFIT_VALUE_DELETE],
)
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
>(managerService)
