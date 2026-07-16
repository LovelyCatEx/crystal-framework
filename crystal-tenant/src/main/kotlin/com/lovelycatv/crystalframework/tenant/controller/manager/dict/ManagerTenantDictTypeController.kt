package com.lovelycatv.crystalframework.tenant.controller.manager.dict

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.ScopedPermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerCreateTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerDeleteTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerReadTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerUpdateTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantDictTypeEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDictTypeRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDictTypeManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/tenant/dict-type")
class ManagerTenantDictTypeController(
    managerService: TenantDictTypeManagerService
) : StandardScopedManagerController<
        TenantDictTypeManagerService,
        TenantDictTypeRepository,
        TenantDictTypeEntity,
        ManagerCreateTenantDictTypeDTO,
        ManagerReadTenantDictTypeDTO,
        ManagerUpdateTenantDictTypeDTO,
        ManagerDeleteTenantDictTypeDTO
>(
    managerService,
    permissions = ScopedPermissionMatrix(
        superCreate = SystemPermission.ACTION_DICT_TYPE_CREATE,
        superRead = SystemPermission.ACTION_DICT_TYPE_READ,
        superUpdate = SystemPermission.ACTION_DICT_TYPE_UPDATE,
        superDelete = SystemPermission.ACTION_DICT_TYPE_DELETE,
        systemCreate = SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE,
        systemRead = SystemPermission.ACTION_SYSTEM_DICT_TYPE_READ,
        systemUpdate = SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE,
        systemDelete = SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE,
        tenantAdminCreate = SystemPermission.ACTION_TENANT_DICT_TYPE_CREATE,
        tenantAdminRead = SystemPermission.ACTION_TENANT_DICT_TYPE_READ,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_DICT_TYPE_UPDATE,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_DICT_TYPE_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_DICT_TYPE_CREATE_PEM,
        tenantPemRead = TenantPermission.ACTION_TENANT_DICT_TYPE_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_DICT_TYPE_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_DICT_TYPE_DELETE_PEM,
    ),
)
