package com.lovelycatv.crystalframework.user.service.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.types.auth.OAuthBindingScope
import com.lovelycatv.crystalframework.shared.types.auth.OAuthPlatform
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.user.converters.OAuth2AuthenticationTokenAccountConverterManager
import com.lovelycatv.crystalframework.user.entity.OAuthAccountEntity
import com.lovelycatv.crystalframework.user.repository.OAuthAccountRepository
import com.lovelycatv.crystalframework.user.service.OAuthAccountService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class OAuthAccountServiceImpl(
    private val oauthAccountRepository: OAuthAccountRepository,
    private val oAuth2AuthenticationTokenAccountConverterManager: OAuth2AuthenticationTokenAccountConverterManager,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : OAuthAccountService {
    private val logger = logger()

    override fun getRepository(): OAuthAccountRepository {
        return this.oauthAccountRepository
    }

    override val cacheStore: ReactiveExpiringKVStore<String, OAuthAccountEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<OAuthAccountEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<OAuthAccountEntity> = OAuthAccountEntity::class

    override suspend fun getAccountsByPlatformAndIdentifier(platform: OAuthPlatform, identifier: String): List<OAuthAccountEntity> {
        return getRepository()
            .findAllByPlatformAndIdentifier(platform.typeId, identifier)
            .awaitListWithTimeout()
    }

    override suspend fun getAccountFromOAuth2AuthenticationToken(token: OAuth2AuthenticationToken): OAuthAccountEntity {
        val template = oAuth2AuthenticationTokenAccountConverterManager.convert(token)

        val existing = this
            .getAccountsByPlatformAndIdentifier(template.getRealPlatform(), template.identifier)
            .firstOrNull { it.getRealScope() == OAuthBindingScope.SYSTEM }

        return if (existing != null) {
            withUpdateEntityContext(existing) {
                withUpdateById(existing.id) {
                    existing.nickname = template.nickname
                }
            }
        } else {
            this.getRepository()
                .save(
                    template.apply {
                        id = snowIdGenerator.nextId()
                        scope = OAuthBindingScope.SYSTEM.typeId
                        tenantId = null
                    } newEntity true
                )
                .awaitFirstOrNull()
                ?: throw BusinessException("Could not save OAuth2 account into database")
        }
    }

    override suspend fun bindUser(accountId: Long, userId: Long) {
        withUpdateEntityContext(accountId) {
            val result = withUpdateById(accountId) {
                if (this.userId != null) {
                    throw BusinessException("This account is already linked to a user")
                }

                // The third-party identity must not already belong to another user (cross-row invariant).
                val conflictingOwner = this@OAuthAccountServiceImpl.getRepository()
                    .findAllByPlatformAndIdentifier(this.platform, this.identifier)
                    .awaitListWithTimeout()
                    .firstOrNull { it.userId != null && it.userId != userId }

                if (conflictingOwner != null) {
                    throw BusinessException("This account already belongs to another user")
                }

                this.userId = userId
            }

            logger.info("OAuth account named ${result.nickname} of platform ${result.getRealPlatform()} has been bound to user $userId")

            result
        }
    }

    override suspend fun unbindUser(accountId: Long, userId: Long) {
        withUpdateEntityContext(accountId) {
            val result = withUpdateById(accountId) {
                if (this.userId != userId) {
                    throw ForbiddenException("You are not allowed to unbind this account")
                }

                this.userId = null
            }

            logger.info("OAuth account named ${result.nickname} of platform ${result.getRealPlatform()} has been unbound by user $userId")

            result
        }
    }

    override suspend fun getUserOAuthAccounts(userId: Long): List<OAuthAccountEntity> {
        return this.getRepository()
            .findAllByUserIdAndScope(userId, OAuthBindingScope.SYSTEM.typeId)
            .awaitListWithTimeout()
    }

    override suspend fun bindTenant(accountId: Long, userId: Long, tenantId: Long): OAuthAccountEntity {
        val source = getByIdOrThrow(accountId, BusinessException("OAuth account $accountId not found"))

        val rows = getRepository()
            .findAllByPlatformAndIdentifier(source.platform, source.identifier)
            .awaitListWithTimeout()

        // Cross-row invariant: the third-party identity may only belong to one user.
        rows.firstOrNull { it.userId != null && it.userId != userId }?.let {
            throw BusinessException("This account already belongs to another user")
        }

        // Idempotency: a tenant may hold at most one binding for this identity.
        rows.firstOrNull {
            it.scope == OAuthBindingScope.TENANT.typeId && it.tenantId == tenantId
        }?.let {
            throw BusinessException("This account is already bound in the current tenant")
        }

        return getRepository().save(
            OAuthAccountEntity(
                id = snowIdGenerator.nextId(),
                userId = userId,
                platform = source.platform,
                identifier = source.identifier,
                nickname = source.nickname,
                avatar = source.avatar,
                email = source.email,
                scope = OAuthBindingScope.TENANT.typeId,
                tenantId = tenantId,
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not bind OAuth account in tenant")
    }

    override suspend fun getUserTenantOAuthAccounts(userId: Long, tenantId: Long): List<OAuthAccountEntity> {
        return getRepository()
            .findAllByUserIdAndScopeAndTenantId(userId, OAuthBindingScope.TENANT.typeId, tenantId)
            .awaitListWithTimeout()
    }

    override suspend fun unbindTenant(accountId: Long, userId: Long, tenantId: Long) {
        withDeleteEntityContext(accountId) {
            val entity = getByIdOrThrow(accountId, BusinessException("OAuth account $accountId not found"))

            if (entity.scope != OAuthBindingScope.TENANT.typeId ||
                entity.tenantId != tenantId ||
                entity.userId != userId
            ) {
                throw ForbiddenException("You are not allowed to unbind this account")
            }

            getRepository().delete(entity).awaitFirstOrNull()

            logger.info("Tenant OAuth account ${entity.nickname} of platform ${entity.getRealPlatform()} unbound by user $userId in tenant $tenantId")
        }
    }

    override suspend fun isAlreadyBindToUser(oauthAccountId: Long): Boolean {
        val account = this.getByIdOrNull(oauthAccountId)
            ?: throw BusinessException("OAuth account not found")

        // Find out other oauth accounts with same plateform and id
        val all = this.getAccountsByPlatformAndIdentifier(account.getRealPlatform(), account.identifier)

        return all.any { it.userId != null }
    }
}