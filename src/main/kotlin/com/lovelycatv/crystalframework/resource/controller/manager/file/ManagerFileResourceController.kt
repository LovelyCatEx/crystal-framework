package com.lovelycatv.crystalframework.resource.controller.manager.file

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerCreateFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerDeleteFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerReadFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerUpdateFileResourceDTO
import com.lovelycatv.crystalframework.resource.service.FileResourceManagerService
import com.lovelycatv.crystalframework.resource.service.FileResourceService
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
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/file-resource")
class ManagerFileResourceController(
    private val fileResourceManagerService: FileResourceManagerService,
) {
    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_FILE_RESOURCE_READ}')")
    @GetMapping("/list", version = "1")
    suspend fun readAllFileResources(
        userAuthentication: UserAuthentication
    ): ApiResponse<*> {
        return ApiResponse.success(fileResourceManagerService.getRepository().findAll().awaitListWithTimeout())
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_FILE_RESOURCE_CREATE}')")
    @PostMapping("/create", version = "1")
    suspend fun createFileResource(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerCreateFileResourceDTO
    ): ApiResponse<*> {
        fileResourceManagerService.create(dto)

        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_FILE_RESOURCE_READ}')")
    @GetMapping("/query", version = "1")
    suspend fun readFileResource(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerReadFileResourceDTO
    ): ApiResponse<*> {
        return ApiResponse.success(fileResourceManagerService.query(dto))
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_FILE_RESOURCE_UPDATE}')")
    @PostMapping("/update", version = "1")
    suspend fun updateFileResource(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerUpdateFileResourceDTO
    ): ApiResponse<*> {
        fileResourceManagerService.update(dto)

        return ApiResponse.success(null)
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_FILE_RESOURCE_DELETE}')")
    @PostMapping("/delete", version = "1")
    suspend fun deleteFileResource(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: ManagerDeleteFileResourceDTO
    ): ApiResponse<*> {
        fileResourceManagerService.deleteByDTO(dto)

        return ApiResponse.success(null)
    }
}
