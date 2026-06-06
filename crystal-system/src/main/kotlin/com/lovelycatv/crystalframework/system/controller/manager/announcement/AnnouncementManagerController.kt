package com.lovelycatv.crystalframework.system.controller.manager.announcement

import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
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

@ManagerPermissions(
    read = [SystemPermission.ACTION_ANNOUNCEMENT_READ],
    readAll = [SystemPermission.ACTION_ANNOUNCEMENT_READ],
    create = [SystemPermission.ACTION_ANNOUNCEMENT_CREATE],
    update = [SystemPermission.ACTION_ANNOUNCEMENT_UPDATE],
    delete = [SystemPermission.ACTION_ANNOUNCEMENT_DELETE],
)
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/announcements")
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
>(managerService)
