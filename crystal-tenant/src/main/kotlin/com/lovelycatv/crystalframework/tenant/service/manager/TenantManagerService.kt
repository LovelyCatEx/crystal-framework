/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerCreateTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerDeleteTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerReadTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerUpdateTenantDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRepository

interface TenantManagerService : CachedBaseManagerService<
        TenantRepository,
        TenantEntity,
        ManagerCreateTenantDTO,
        ManagerReadTenantDTO,
        ManagerUpdateTenantDTO,
        ManagerDeleteTenantDTO
>
