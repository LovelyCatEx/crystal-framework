package com.lovelycatv.crystalframework.system.controller

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.system.entity.AnnouncementEntity
import com.lovelycatv.crystalframework.system.service.AnnouncementService
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/announcements")
class AnnouncementController(
    private val announcementService: AnnouncementService,
) {
    @PreAuthorize("hasAuthority('${SystemPermission.ACTION_ANNOUNCEMENT_LIST}')")
    @GetMapping("/list")
    suspend fun list(): ApiResponse<List<AnnouncementEntity>> {
        val list = announcementService.getPublished().collectList().awaitFirst()
        return ApiResponse.success(list)
    }
}
