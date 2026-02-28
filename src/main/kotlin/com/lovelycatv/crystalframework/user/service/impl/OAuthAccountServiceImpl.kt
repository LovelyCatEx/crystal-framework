package com.lovelycatv.crystalframework.user.service.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.user.converter.OAuth2AuthenticationTokenAccountConverterManager
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.user.repository.OAuthAccountRepository
import com.lovelycatv.crystalframework.user.service.OAuthAccountService
import com.lovelycatv.crystalframework.user.types.OAuthPlatform
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service

@Service
class OAuthAccountServiceImpl(
    private val oauthAccountRepository: OAuthAccountRepository,
    private val oAuth2AuthenticationTokenAccountConverterManager: OAuth2AuthenticationTokenAccountConverterManager,
    private val snowIdGenerator: SnowIdGenerator
) : OAuthAccountService {
    override fun getRepository(): OAuthAccountRepository {
        return this.oauthAccountRepository
    }

    override suspend fun getAccountByPlatformAndIdentifier(platform: OAuthPlatform, identifier: String): OAuthAccountEntity? {
        return this.getRepository()
            .findByPlatformAndIdentifier(platform.typeId, identifier)
            .awaitFirstOrNull()
    }

    override suspend fun getAccountFromOAuth2AuthenticationToken(token: OAuth2AuthenticationToken): OAuthAccountEntity {
        val template = oAuth2AuthenticationTokenAccountConverterManager.convert(token)

        val existing = this.getAccountByPlatformAndIdentifier(template.getRealPlatform(), template.identifier)

        return existing
            ?: (this.getRepository()
                .save(
                    template.apply {
                        id = snowIdGenerator.nextId()
                    } newEntity true
                )
                .awaitFirstOrNull()
                ?: throw BusinessException("could not save oauth2 account into database"))
    }
}