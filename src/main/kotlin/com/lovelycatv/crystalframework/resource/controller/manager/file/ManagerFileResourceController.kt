package com.lovelycatv.crystalframework.resource.controller.manager.file

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerCreateFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerDeleteFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerReadFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerUpdateFileResourceDTO
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.resource.service.FileResourceManagerService
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = SystemPermission.ACTION_FILE_RESOURCE_READ,
    readAll = SystemPermission.ACTION_FILE_RESOURCE_READ,
    create = SystemPermission.ACTION_FILE_RESOURCE_CREATE,
    update = SystemPermission.ACTION_FILE_RESOURCE_UPDATE,
    delete = SystemPermission.ACTION_FILE_RESOURCE_DELETE,
)
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
>(managerService)
