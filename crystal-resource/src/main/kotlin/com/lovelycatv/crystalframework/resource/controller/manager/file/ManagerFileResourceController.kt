package com.lovelycatv.crystalframework.resource.controller.manager.file

import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerCreateFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerDeleteFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerReadFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerUpdateFileResourceDTO
import com.lovelycatv.crystalframework.resource.constants.FileResourcePermissions
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.resource.service.manager.FileResourceManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.ManagerAction
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/file-resource")
class ManagerFileResourceController(
    managerService: FileResourceManagerService
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
}
