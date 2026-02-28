package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.shared.service.BaseService
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.user.repository.OAuthAccountRepository
import com.lovelycatv.crystalframework.user.types.OAuthPlatform
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken

interface OAuthAccountService : BaseService<OAuthAccountRepository, OAuthAccountEntity> {
    suspend fun getAccountByPlatformAndIdentifier(platform: OAuthPlatform, identifier: String): OAuthAccountEntity?

    suspend fun getAccountFromOAuth2AuthenticationToken(token: OAuth2AuthenticationToken): OAuthAccountEntity
}