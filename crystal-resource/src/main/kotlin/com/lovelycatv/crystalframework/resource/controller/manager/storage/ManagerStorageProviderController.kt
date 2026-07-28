package com.lovelycatv.crystalframework.resource.controller.manager.storage

import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerCreateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerDeleteStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerReadStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerUpdateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRepository
import com.lovelycatv.crystalframework.resource.service.StorageProviderManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/storage-provider")
class ManagerStorageProviderController(
    managerService: StorageProviderManagerService
) : StandardManagerController<
        StorageProviderManagerService,
        StorageProviderRepository,
        StorageProviderEntity,
        ManagerCreateStorageProviderDTO,
        ManagerReadStorageProviderDTO,
        ManagerUpdateStorageProviderDTO,
        ManagerDeleteStorageProviderDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = PermissionMatrix.NOT_APPLICABLE,
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        systemUpdate = PermissionMatrix.NOT_APPLICABLE,
        systemDelete = PermissionMatrix.NOT_APPLICABLE,
        superCreate = SystemPermission.ACTION_STORAGE_PROVIDER_CREATE,
        superRead = SystemPermission.ACTION_STORAGE_PROVIDER_READ,
        superUpdate = SystemPermission.ACTION_STORAGE_PROVIDER_UPDATE,
        superDelete = SystemPermission.ACTION_STORAGE_PROVIDER_DELETE,
    ),
)
