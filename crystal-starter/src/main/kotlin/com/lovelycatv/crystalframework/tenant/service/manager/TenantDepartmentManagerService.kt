package com.lovelycatv.crystalframework.tenant.service.manager

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
