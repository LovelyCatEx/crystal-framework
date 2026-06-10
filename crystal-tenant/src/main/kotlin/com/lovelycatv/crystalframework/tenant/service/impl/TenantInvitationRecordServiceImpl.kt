package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationRecordEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRecordRepository
import com.lovelycatv.crystalframework.tenant.service.TenantInvitationRecordService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantInvitationRecordServiceImpl(
    private val tenantInvitationRecordRepository: TenantInvitationRecordRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val snowIdGenerator: SnowIdGenerator,
) : TenantInvitationRecordService {
    override val cacheStore: ReactiveExpiringKVStore<String, TenantInvitationRecordEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantInvitationRecordEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantInvitationRecordEntity> = TenantInvitationRecordEntity::class

    override fun getRepository(): TenantInvitationRecordRepository {
        return this.tenantInvitationRecordRepository
    }

    override suspend fun saveRecord(
        invitationId: Long,
        userId: Long,
        realName: String,
        phoneNumber: String
    ): TenantInvitationRecordEntity {
        return this.getRepository().save(
            TenantInvitationRecordEntity(
                id = snowIdGenerator.nextId(),
                invitationId = invitationId,
                usedUserId = userId,
                realName = realName,
                phoneNumber = phoneNumber
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("could not save invitation record")
    }

    override suspend fun getAllByInvitationId(invitationId: Long): List<TenantInvitationRecordEntity> {
        return this.getRepository()
            .findAllByInvitationId(invitationId)
            .awaitListWithTimeout()
    }

    override suspend fun getAllByUsedUserId(usedUserId: Long): List<TenantInvitationRecordEntity> {
        return this.getRepository()
            .findAllByUsedUserId(usedUserId)
            .awaitListWithTimeout()
    }
}
