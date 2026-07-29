package com.lovelycatv.crystalframework.auth.controller

import com.lovelycatv.crystalframework.auth.controller.dto.UserSwitchAuthenticationDTO
import com.lovelycatv.crystalframework.auth.service.UserAuthorizationService
import com.lovelycatv.crystalframework.auth.service.impl.CustomUserDetailsService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.service.UserService
import com.lovelycatv.vertex.log.logger
import jakarta.validation.Valid
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
    private val userService: UserService,
    private val customUserDetailsService: CustomUserDetailsService
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
        val userWithTenant = customUserDetailsService
            .findByUsername("${user.username}:${dto.tenantId}")
            .awaitFirstOrNull()
            as? UserEntity?
            ?: throw BusinessException("target tenant not found or you are not a member of it")

        userAuthorizationService.clearUserAuthorityCache(userAuthentication.userId)

        // Fixed: Using legacy redisService instead of reactiveRedisService
        // Prevents dead redis lock produces consistent blocking
        // delay(100.milliseconds)

        logger.info("user ${user.username} - ${user.id} is switched tenant authentication to ${dto.tenantId}")

        return ApiResponse.success(userAuthorizationService.buildLoginSuccessResponse(userWithTenant))
    }
}