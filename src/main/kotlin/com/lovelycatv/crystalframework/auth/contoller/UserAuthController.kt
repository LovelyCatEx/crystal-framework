package com.lovelycatv.crystalframework.auth.contoller

import com.lovelycatv.crystalframework.auth.contoller.dto.UserSwitchAuthenticationDTO
import com.lovelycatv.crystalframework.auth.service.UserAuthorizationService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.service.UserService
import com.lovelycatv.vertex.log.logger
import jakarta.validation.Valid
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/user/auth")
class UserAuthController(
    private val userAuthorizationService: UserAuthorizationService,
    private val userService: UserService
) {
    private val logger = logger()

    @PostMapping("/switchTenant")
    suspend fun switchAuthentication(
        userAuthentication: UserAuthentication,
        @ModelAttribute
        @Valid
        dto: UserSwitchAuthenticationDTO
    ): ApiResponse<*> {
        val user = userService.getByIdOrThrow(userAuthentication.userId)

        // To get the authenticated tenant
        val userWithTenant = userService
            .findByUsername("${user.username}:${dto.tenantId}")
            .awaitFirstOrNull()
            as? UserEntity?

        userAuthorizationService.clearUserAuthorityCache(userAuthentication.userId)

        // Prevents dead redis lock produces consistent blocking
        delay(100)

        return ApiResponse.success(
            userWithTenant?.let {
                userAuthorizationService.buildLoginSuccessResponse(it).also {
                    logger.info("user ${user.username} - ${user.id} is switched tenant authentication to ${dto.tenantId}")
                }
            } ?: userAuthorizationService.buildLoginSuccessResponse(user).also {
                logger.warn("could not switch tenant authentication for user ${user.username} - ${user.id}, " +
                        "target tenant ${dto.tenantId} not found or not the member of this tenant"
                )
            }
        )
    }
}