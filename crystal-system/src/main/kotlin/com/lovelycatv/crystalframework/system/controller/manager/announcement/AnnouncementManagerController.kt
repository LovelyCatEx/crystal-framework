package com.lovelycatv.crystalframework.system.controller.manager.announcement

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import com.lovelycatv.crystalframework.system.controller.manager.announcement.dto.ManagerCreateAnnouncementDTO
import com.lovelycatv.crystalframework.system.controller.manager.announcement.dto.ManagerDeleteAnnouncementDTO
import com.lovelycatv.crystalframework.system.controller.manager.announcement.dto.ManagerReadAnnouncementDTO
import com.lovelycatv.crystalframework.system.controller.manager.announcement.dto.ManagerUpdateAnnouncementDTO
import com.lovelycatv.crystalframework.system.entity.AnnouncementEntity
import com.lovelycatv.crystalframework.system.repository.AnnouncementRepository
import com.lovelycatv.crystalframework.system.service.manager.AnnouncementManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/announcement")
class AnnouncementManagerController(
    managerService: AnnouncementManagerService,
) : StandardManagerController<
        AnnouncementManagerService,
        AnnouncementRepository,
        AnnouncementEntity,
        ManagerCreateAnnouncementDTO,
        ManagerReadAnnouncementDTO,
        ManagerUpdateAnnouncementDTO,
        ManagerDeleteAnnouncementDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = PermissionMatrix.NOT_APPLICABLE,
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        systemUpdate = PermissionMatrix.NOT_APPLICABLE,
        systemDelete = PermissionMatrix.NOT_APPLICABLE,
        superCreate = SystemPermission.ACTION_ANNOUNCEMENT_CREATE,
        superRead = SystemPermission.ACTION_ANNOUNCEMENT_READ,
        superUpdate = SystemPermission.ACTION_ANNOUNCEMENT_UPDATE,
        superDelete = SystemPermission.ACTION_ANNOUNCEMENT_DELETE,
    ),
)
