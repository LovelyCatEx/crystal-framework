package com.lovelycatv.crystalframework.rbac.tenant.service.manager

import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantRoleDeclaration
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.ManagerCreateTenantRoleDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.ManagerDeleteTenantRoleDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.ManagerReadTenantRoleDTO
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.ManagerUpdateTenantRoleDTO
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantRoleRepository
import com.lovelycatv.crystalframework.shared.service.BaseTenantResourceManagerService

interface TenantRoleManagerService : BaseTenantResourceManagerService<
        TenantRoleRepository,
        TenantRoleEntity,
        ManagerCreateTenantRoleDTO,
        ManagerReadTenantRoleDTO,
        ManagerUpdateTenantRoleDTO,
        ManagerDeleteTenantRoleDTO
        > {
    suspend fun createFromDeclaration(tenantId: Long, declaration: TenantRoleDeclaration): TenantRoleEntity
}
