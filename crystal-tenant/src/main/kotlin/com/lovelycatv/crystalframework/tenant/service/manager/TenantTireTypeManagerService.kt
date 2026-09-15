/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerCreateTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerDeleteTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerReadTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerUpdateTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantTireTypeEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireTypeRepository

interface TenantTireTypeManagerService : CachedBaseManagerService<
        TenantTireTypeRepository,
        TenantTireTypeEntity,
        ManagerCreateTenantTireTypeDTO,
        ManagerReadTenantTireTypeDTO,
        ManagerUpdateTenantTireTypeDTO,
        ManagerDeleteTenantTireTypeDTO
>
