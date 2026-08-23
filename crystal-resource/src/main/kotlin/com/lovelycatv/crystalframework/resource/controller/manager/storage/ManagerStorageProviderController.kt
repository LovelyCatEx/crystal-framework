package com.lovelycatv.crystalframework.resource.controller.manager.storage

import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerCreateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerDeleteStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerReadStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerUpdateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.vo.StorageProviderTypeVO
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRepository
import com.lovelycatv.crystalframework.resource.service.manager.StorageProviderManagerService
import com.lovelycatv.crystalframework.sdk.resource.storage.StorageProviderTypeRegistry
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/storage-provider")
class ManagerStorageProviderController(
    managerService: StorageProviderManagerService,
    private val storageProviderTypeRegistry: StorageProviderTypeRegistry,
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
) {

    /**
     * Every [com.lovelycatv.crystalframework.sdk.resource.storage.types.StorageProviderTypeDeclaration]
     * currently registered — built-ins first (`typeId < 1000`), third-party contributions after.
     * The admin UI uses this to render the type dropdown so contributed types show up on equal
     * footing without frontend code changes. Read-permission gated: seeing the list is a strict
     * subset of seeing providers.
     */
    @RequiresAuthority(anyOf = ["system.storage.provider.read"], scope = ResourceScope.SYSTEM)
    @GetMapping("/types")
    suspend fun listTypes(): ApiResponse<List<StorageProviderTypeVO>> {
        val list = storageProviderTypeRegistry.declarations().map {
            StorageProviderTypeVO(
                typeId = it.typeId,
                key = it.key,
                displayName = it.displayName,
                description = it.description,
            )
        }
        return ApiResponse.success(list)
    }
}
