package com.lovelycatv.crystalframework.resource.controller.manager.storage

import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerCreateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerDeleteStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerReadStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerUpdateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRepository
import com.lovelycatv.crystalframework.resource.service.manager.StorageProviderManagerService
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
        systemCreate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_DELETE.name,
    ),
)
