package com.lovelycatv.crystalframework.tenant.controller.manager.dict

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
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
    permissions = PermissionMatrix(
        superCreate = SystemPermission.ACTION_X_DICT_TYPE_CREATE.name,
        superRead = SystemPermission.ACTION_X_DICT_TYPE_READ.name,
        superUpdate = SystemPermission.ACTION_X_DICT_TYPE_UPDATE.name,
        superDelete = SystemPermission.ACTION_X_DICT_TYPE_DELETE.name,
        systemCreate = SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_DICT_TYPE_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE.name,
        tenantAdminCreate = SystemPermission.ACTION_TENANT_DICT_TYPE_CREATE.name,
        tenantAdminRead = SystemPermission.ACTION_TENANT_DICT_TYPE_READ.name,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_DICT_TYPE_UPDATE.name,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_DICT_TYPE_DELETE.name,
        tenantPemCreate = TenantPermission.ACTION_DICT_TYPE_CREATE.name,
        tenantPemRead = TenantPermission.ACTION_DICT_TYPE_READ.name,
        tenantPemUpdate = TenantPermission.ACTION_DICT_TYPE_UPDATE.name,
        tenantPemDelete = TenantPermission.ACTION_DICT_TYPE_DELETE.name,
    ),
)
