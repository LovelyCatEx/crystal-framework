package com.lovelycatv.crystalframework.auth.types

import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.user.entity.UserEntity

data class ProcessOAuth2AuthenticationSuccessResult(
    val user: UserEntity?,
    val oauth2Account: OAuthAccountEntity?,
    val response: ApiResponse<*>
)