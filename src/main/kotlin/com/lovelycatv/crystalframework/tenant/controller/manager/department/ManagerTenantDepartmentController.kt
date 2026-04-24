package com.lovelycatv.crystalframework.tenant.controller.manager.department

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.tenant.controller.manager.StandardTenantManagerController
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
    createPermission = SystemPermission.ACTION_TENANT_DEPARTMENT_CREATE,
    scopedCreatePermission = TenantPermission.ACTION_TENANT_DEPARTMENT_CREATE_PEM,
    readPermission = SystemPermission.ACTION_TENANT_DEPARTMENT_READ,
    scopedReadPermission = TenantPermission.ACTION_TENANT_DEPARTMENT_READ_PEM,
    updatePermission = SystemPermission.ACTION_TENANT_DEPARTMENT_UPDATE,
    scopedUpdatePermission = TenantPermission.ACTION_TENANT_DEPARTMENT_UPDATE_PEM,
    deletePermission = SystemPermission.ACTION_TENANT_DEPARTMENT_DELETE,
    scopedDeletePermission = TenantPermission.ACTION_TENANT_DEPARTMENT_DELETE_PEM
)
