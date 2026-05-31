package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerDeleteTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.vo.ManagerReadTenantTireBenefitOverviewItemVO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerReadTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerUpdateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantTireBenefitValueEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireBenefitValueRepository

interface TenantTireBenefitValueManagerService : CachedBaseManagerService<
        TenantTireBenefitValueRepository,
        TenantTireBenefitValueEntity,
        ManagerCreateTenantTireBenefitValueDTO,
        ManagerReadTenantTireBenefitValueDTO,
        ManagerUpdateTenantTireBenefitValueDTO,
        ManagerDeleteTenantTireBenefitValueDTO
>