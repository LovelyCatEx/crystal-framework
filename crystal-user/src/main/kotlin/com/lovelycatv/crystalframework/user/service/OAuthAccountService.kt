package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.user.repository.OAuthAccountRepository
import com.lovelycatv.crystalframework.shared.types.auth.OAuthPlatform
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken

interface OAuthAccountService : CachedBaseService<OAuthAccountRepository, OAuthAccountEntity> {
    suspend fun getAccountsByPlatformAndIdentifier(platform: OAuthPlatform, identifier: String): List<OAuthAccountEntity>

    suspend fun getAccountFromOAuth2AuthenticationToken(token: OAuth2AuthenticationToken): OAuthAccountEntity

    suspend fun bindUser(accountId: Long, userId: Long)

    suspend fun unbindUser(accountId: Long, userId: Long)

    suspend fun getUserOAuthAccounts(userId: Long): List<OAuthAccountEntity>

    /**
     * Binds a third-party identity (resolved from [accountId], an existing row of any scope) to
     * [userId] within [tenantId]. Enforces the cross-row invariant that the identity belongs to
     * a single user, and idempotency per (platform, identifier, TENANT, tenantId).
     */
    suspend fun bindTenant(accountId: Long, userId: Long, tenantId: Long): OAuthAccountEntity

    /**
     * Tenant-scoped bindings owned by [userId] within [tenantId].
     */
    suspend fun getUserTenantOAuthAccounts(userId: Long, tenantId: Long): List<OAuthAccountEntity>

    /**
     * Unbinds a tenant-scoped binding. The row must belong to [userId] and [tenantId].
     */
    suspend fun unbindTenant(accountId: Long, userId: Long, tenantId: Long)

    suspend fun isAlreadyBindToUser(oauthAccountId: Long): Boolean
}