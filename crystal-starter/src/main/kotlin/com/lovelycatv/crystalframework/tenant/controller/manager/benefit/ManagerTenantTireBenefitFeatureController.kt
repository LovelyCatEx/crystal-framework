package com.lovelycatv.crystalframework.tenant.controller.manager.benefit

import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitFeatureDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerDeleteTenantTireBenefitFeatureDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerReadTenantTireBenefitFeatureDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerUpdateTenantTireBenefitFeatureDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantTireBenefitFeatureEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitFeatureRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantTireBenefitFeatureManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = [SystemPermission.ACTION_TENANT_TIRE_BENEFIT_FEATURE_READ],
    readAll = [SystemPermission.ACTION_TENANT_TIRE_BENEFIT_FEATURE_READ],
    create = [SystemPermission.ACTION_TENANT_TIRE_BENEFIT_FEATURE_CREATE],
    update = [SystemPermission.ACTION_TENANT_TIRE_BENEFIT_FEATURE_UPDATE],
    delete = [SystemPermission.ACTION_TENANT_TIRE_BENEFIT_FEATURE_DELETE],
)
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/tire/benefit/feature")
class ManagerTenantTireBenefitFeatureController(
    managerService: TenantTireBenefitFeatureManagerService
) : StandardManagerController<
        TenantTireBenefitFeatureManagerService,
        TenantTireBenefitFeatureRepository,
        TenantTireBenefitFeatureEntity,
        ManagerCreateTenantTireBenefitFeatureDTO,
        ManagerReadTenantTireBenefitFeatureDTO,
        ManagerUpdateTenantTireBenefitFeatureDTO,
        ManagerDeleteTenantTireBenefitFeatureDTO
>(managerService)
