package com.lovelycatv.crystalframework.tenant.controller

import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.tenant.controller.vo.UserTenantVO
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.tenant.types.TenantStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/tenant")
class UserTenantController(
    private val tenantService: TenantService,
    private val fileResourceService: FileResourceService,
) {
    @GetMapping("/joined")
    suspend fun getUserTenants(
        userAuthentication: UserAuthentication
    ): ApiResponse<*> {
        val data = tenantService
            .getUserTenants(userAuthentication.userId)
            .filter { it.getRealStatus() == TenantStatus.ACTIVE }
            .map {
                UserTenantVO(
                    tenantId = it.id,
                    tenantName = it.name,
                    tenantAvatar = fileResourceService.getFileDownloadUrl(it.icon),
                    authenticated = userAuthentication.tenantId == it.id
                )
            }

        return ApiResponse.success(data)
    }
}