package com.lovelycatv.crystalframework.rbac.user.controller

import com.lovelycatv.crystalframework.rbac.user.controller.vo.UserAccessibleResourceVO
import com.lovelycatv.crystalframework.rbac.user.service.UserRbacQueryService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/user/menus")
class UserAccessibleMenuController(
    private val userRbacQueryService: UserRbacQueryService
) {
    @GetMapping("/list")
    suspend fun listAccessibleMenus(userAuthentication: UserAuthentication): ApiResponse<*> {
        val userRbac = userRbacQueryService.getUserRbacAccessInfo(userAuthentication.userId)

        val tenantRbac = if (userAuthentication.tenantId != null && userAuthentication.tenantMemberId != null) {
            userRbacQueryService.getTenantMemberRbacAccessInfo(
                tenantMemberId = userAuthentication.tenantMemberId!!,
                tenantId = userAuthentication.tenantId!!
            )
        } else null

        val userMenus = userRbac.paths.mapNotNull { it.path }
        val tenantMenus = (tenantRbac?.permissions?.mapNotNull { it.path } ?: emptyList())

        val userComponents = userRbac.components.mapNotNull { it.path }

        return ApiResponse.success(
            UserAccessibleResourceVO(
                menus = userMenus + tenantMenus,
                components = userComponents
            )

        )
    }
}