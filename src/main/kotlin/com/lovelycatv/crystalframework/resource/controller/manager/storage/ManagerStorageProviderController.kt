package com.lovelycatv.crystalframework.resource.controller.manager.storage

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerCreateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerDeleteStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerReadStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerUpdateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRepository
import com.lovelycatv.crystalframework.resource.service.StorageProviderManagerService
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = [SystemPermission.ACTION_STORAGE_PROVIDER_READ],
    readAll = [SystemPermission.ACTION_STORAGE_PROVIDER_READ],
    create = [SystemPermission.ACTION_STORAGE_PROVIDER_CREATE],
    update = [SystemPermission.ACTION_STORAGE_PROVIDER_UPDATE],
    delete = [SystemPermission.ACTION_STORAGE_PROVIDER_DELETE],
)
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
>(managerService)
