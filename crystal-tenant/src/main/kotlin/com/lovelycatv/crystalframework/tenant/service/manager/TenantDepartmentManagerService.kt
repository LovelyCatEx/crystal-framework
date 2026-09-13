/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.shared.service.BaseTenantResourceManagerService
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerCreateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerDeleteTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerReadTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerUpdateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentRepository

interface TenantDepartmentManagerService : BaseTenantResourceManagerService<
        TenantDepartmentRepository,
        TenantDepartmentEntity,
        ManagerCreateTenantDepartmentDTO,
        ManagerReadTenantDepartmentDTO,
        ManagerUpdateTenantDepartmentDTO,
        ManagerDeleteTenantDepartmentDTO
        >
