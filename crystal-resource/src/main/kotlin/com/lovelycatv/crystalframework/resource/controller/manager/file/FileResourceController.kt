package com.lovelycatv.crystalframework.resource.controller.manager.file

import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/file-resource")
class FileResourceController(
    private val fileResourceService: FileResourceService
) {
    @GetMapping("/downloadUrl", version = "1")
    suspend fun getFileDownloadUrl(
        userAuthentication: UserAuthentication,
        @RequestParam(value = "id") id: Long
    ): ApiResponse<*> {
        return ApiResponse.success(fileResourceService.getFileDownloadUrl(id))
    }
}