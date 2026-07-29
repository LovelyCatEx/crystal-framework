package com.lovelycatv.crystalframework.tenant.controller.manager.department

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardTenantManagerController
import com.lovelycatv.crystalframework.shared.controller.tenantOnly
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerCreateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerDeleteTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerReadTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerUpdateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/department")
class ManagerTenantDepartmentController(
    managerService: TenantDepartmentManagerService
): StandardTenantManagerController<
        TenantDepartmentManagerService,
        TenantDepartmentRepository,
        TenantDepartmentEntity,
        ManagerCreateTenantDepartmentDTO,
        ManagerReadTenantDepartmentDTO,
        ManagerUpdateTenantDepartmentDTO,
        ManagerDeleteTenantDepartmentDTO
>(
    managerService,
    permissions = PermissionMatrix.tenantOnly(
        tenantAdminCreate = SystemPermission.ACTION_TENANT_DEPARTMENT_CREATE.name,
        tenantAdminRead = SystemPermission.ACTION_TENANT_DEPARTMENT_READ.name,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_DEPARTMENT_UPDATE.name,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_DEPARTMENT_DELETE.name,
        tenantPemCreate = TenantPermission.ACTION_DEPARTMENT_CREATE.name,
        tenantPemRead = TenantPermission.ACTION_DEPARTMENT_READ.name,
        tenantPemUpdate = TenantPermission.ACTION_DEPARTMENT_UPDATE.name,
        tenantPemDelete = TenantPermission.ACTION_DEPARTMENT_DELETE.name,
    ),
)
