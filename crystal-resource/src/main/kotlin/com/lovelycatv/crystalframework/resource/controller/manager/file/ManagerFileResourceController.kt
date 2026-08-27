package com.lovelycatv.crystalframework.resource.controller.manager.file

import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerCreateFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerDeleteFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerReadFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerUpdateFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.vo.ResourceFileTypeVO
import com.lovelycatv.crystalframework.resource.constants.FileResourcePermissions
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.resource.service.manager.FileResourceManagerService
import com.lovelycatv.crystalframework.sdk.resource.file.ResourceFileTypeRegistry
import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.ManagerAction
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/file-resource")
class ManagerFileResourceController(
    managerService: FileResourceManagerService,
    private val resourceFileTypeRegistry: ResourceFileTypeRegistry,
) : StandardScopedManagerController<
        FileResourceManagerService,
        FileResourceRepository,
        FileResourceEntity,
        ManagerCreateFileResourceDTO,
        ManagerReadFileResourceDTO,
        ManagerUpdateFileResourceDTO,
        ManagerDeleteFileResourceDTO
>(
    managerService,
    permissions = FileResourcePermissions.MATRIX,
) {
    override suspend fun preflight(
        action: ManagerAction,
        userAuthentication: UserAuthentication,
        createDto: ManagerCreateFileResourceDTO?,
        readDto: ManagerReadFileResourceDTO?,
        updateDto: ManagerUpdateFileResourceDTO?,
        deleteDto: ManagerDeleteFileResourceDTO?,
    ): ApiResponse<*>? = when (action) {
        ManagerAction.CREATE -> {
            val (scope, _) = resolveScopeFromCreateDTO(createDto!!)
            throw ForbiddenException(
                "File resources cannot be created through the manager API",
                context = ForbiddenContext(
                    reason = ForbiddenReason.PROTECTED_RESOURCE,
                    scope = scope,
                ),
            )
        }
        ManagerAction.UPDATE -> {
            val entity = managerService.getByIdOrThrow(updateDto!!.id)
            val (scope, _) = resolveScopeFromEntity(entity)
            throw ForbiddenException(
                "File resources cannot be updated through the manager API",
                context = ForbiddenContext(
                    reason = ForbiddenReason.PROTECTED_RESOURCE,
                    scope = scope,
                ),
            )
        }
        ManagerAction.READ,
        ManagerAction.DELETE,
        ManagerAction.READ_ALL -> null
    }

    /**
     * Every [com.lovelycatv.crystalframework.sdk.resource.file.types.ResourceFileTypeDeclaration]
     * currently registered — built-ins first (`typeId < 1000`), third-party contributions after.
     * The admin UI uses this to render the file-type dropdown so contributed types show up on
     * equal footing without frontend code changes. Read-permission gated: seeing the list is a
     * strict subset of seeing file resources.
     */
    @RequiresAuthority(anyOf = ["system.file.resource.read"], scope = ResourceScope.SYSTEM)
    @GetMapping("/types")
    suspend fun listTypes(): ApiResponse<List<ResourceFileTypeVO>> {
        val list = resourceFileTypeRegistry.declarations().map {
            ResourceFileTypeVO(
                typeId = it.typeId,
                key = it.key,
                displayName = it.displayName,
                description = it.description,
                supportedContentTypes = it.supportedContentTypes,
                supportedFileExtensions = it.supportedFileExtensions,
                defaultVisibility = it.defaultVisibility,
            )
        }
        return ApiResponse.success(list)
    }
}
