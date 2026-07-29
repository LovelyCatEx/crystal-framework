package com.lovelycatv.crystalframework.tenant.controller.manager.benefit

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
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
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_TENANT_TIRE_BENEFIT_FEATURE_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_TENANT_TIRE_BENEFIT_FEATURE_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_TENANT_TIRE_BENEFIT_FEATURE_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_TENANT_TIRE_BENEFIT_FEATURE_DELETE.name,
    ),
)
