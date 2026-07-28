package com.lovelycatv.crystalframework.resource.controller.manager.file

import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerCreateFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerDeleteFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerReadFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerUpdateFileResourceDTO
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.resource.service.FileResourceManagerService
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
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/file-resource")
class ManagerFileResourceController(
    managerService: FileResourceManagerService
) : StandardManagerController<
        FileResourceManagerService,
        FileResourceRepository,
        FileResourceEntity,
        ManagerCreateFileResourceDTO,
        ManagerReadFileResourceDTO,
        ManagerUpdateFileResourceDTO,
        ManagerDeleteFileResourceDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = PermissionMatrix.NOT_APPLICABLE,
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        systemUpdate = PermissionMatrix.NOT_APPLICABLE,
        systemDelete = PermissionMatrix.NOT_APPLICABLE,
        superCreate = SystemPermission.ACTION_FILE_RESOURCE_CREATE,
        superRead = SystemPermission.ACTION_FILE_RESOURCE_READ,
        superUpdate = SystemPermission.ACTION_FILE_RESOURCE_UPDATE,
        superDelete = SystemPermission.ACTION_FILE_RESOURCE_DELETE,
    ),
)
