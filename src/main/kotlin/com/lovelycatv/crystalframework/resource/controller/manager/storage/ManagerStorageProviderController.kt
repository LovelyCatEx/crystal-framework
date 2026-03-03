package com.lovelycatv.crystalframework.resource.controller.manager.storage

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerCreateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerDeleteStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerReadStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerUpdateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.service.StorageProviderManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/storage-provider")
class ManagerStorageProviderController(
    private val storageProviderManagerService: StorageProviderManagerService
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_STORAGE_PROVIDER_READ}')")
    @GetMapping("/list", version = "1")
    suspend fun readAllStorageProviders(
        userAuthentication: UserAuthentication
    ): ApiResponse<*> {
        return ApiResponse.success(storageProviderManagerService.getRepository().findAll().awaitListWithTimeout())
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_STORAGE_PROVIDER_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun createStorageProvider(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateStorageProviderDTO
    ): ApiResponse<*> {
        storageProviderManagerService.create(dto)

        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_STORAGE_PROVIDER_READ}')")
    @GetMapping("/query", version = "1")
    suspend fun readStorageProvider(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerReadStorageProviderDTO
    ): ApiResponse<*> {
        return ApiResponse.success(storageProviderManagerService.query(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_STORAGE_PROVIDER_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun updateStorageProvider(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdateStorageProviderDTO
    ): ApiResponse<*> {
        storageProviderManagerService.update(dto)

        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_STORAGE_PROVIDER_DELETE}')")
    @PostMapping("/delete", version = "1")
    suspend fun deleteStorageProvider(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerDeleteStorageProviderDTO
    ): ApiResponse<*> {
        storageProviderManagerService.deleteByDTO(dto)

        return ApiResponse.success(null)
    }
}
