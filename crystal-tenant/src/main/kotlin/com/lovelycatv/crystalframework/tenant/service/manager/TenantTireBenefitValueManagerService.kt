/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerCreateTenantTireBenefitValueDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto.ManagerDeleteTenantTireBenefitValueDTO
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