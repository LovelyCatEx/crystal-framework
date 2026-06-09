package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberProfileEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberProfileRepository
import com.lovelycatv.crystalframework.tenant.service.TenantMemberProfileService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantMemberProfileServiceImpl(
    private val tenantMemberProfileRepository: TenantMemberProfileRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val snowIdGenerator: SnowIdGenerator,
) : TenantMemberProfileService {
    override val cacheStore: ReactiveExpiringKVStore<String, TenantMemberProfileEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantMemberProfileEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantMemberProfileEntity> = TenantMemberProfileEntity::class

    override fun getRepository(): TenantMemberProfileRepository {
        return this.tenantMemberProfileRepository
    }

    override suspend fun getByTenantMemberId(tenantMemberId: Long): TenantMemberProfileEntity? {
        return this.getRepository()
            .findByTenantMemberId(tenantMemberId)
            .awaitFirstOrNull()
    }

    override suspend fun getByTenantIdAndUserId(tenantId: Long, userId: Long): TenantMemberProfileEntity? {
        return this.getRepository()
            .findByTenantIdAndMemberUserId(tenantId, userId)
            .awaitFirstOrNull()
    }

    override suspend fun upsertProfile(
        tenantId: Long,
        tenantMemberId: Long,
        memberUserId: Long,
        name: String?,
        phone: String?,
        nickname: String?,
        avatar: Long?,
        email: String?,
        bio: String?,
        gender: Int?,
        birthday: Long?,
        timezone: String?,
        locale: String?,
    ): TenantMemberProfileEntity {
        val existing = this.getByTenantMemberId(tenantMemberId)

        return if (existing == null) {
            val entity = TenantMemberProfileEntity(
                id = snowIdGenerator.nextId(),
                tenantId = tenantId,
                tenantMemberId = tenantMemberId,
                memberUserId = memberUserId,
                name = name ?: "",
                phone = phone ?: "",
                nickname = nickname,
                avatar = avatar,
                email = email,
                bio = bio,
                gender = gender,
                birthday = birthday,
                timezone = timezone,
                locale = locale,
            ).apply { newEntity() }

            val saved = this.getRepository().save(entity).awaitFirstOrNull()
                ?: throw BusinessException("could not create tenant user profile")

            this.updateCache(saved)
            saved
        } else {
            this.withUpdateEntityContext(existing) {
                existing.apply {
                    // name is write-once: ignored after creation.
                    phone?.let { this.phone = it }
                    nickname?.let { this.nickname = it }
                    avatar?.let { this.avatar = it }
                    email?.let { this.email = it }
                    bio?.let { this.bio = it }
                    gender?.let { this.gender = it }
                    birthday?.let { this.birthday = it }
                    timezone?.let { this.timezone = it }
                    locale?.let { this.locale = it }
                    onUpdate()
                }
                this.getRepository().save(existing).awaitFirstOrNull()
                    ?: throw BusinessException("could not update tenant user profile")
            }
        }
    }
}
