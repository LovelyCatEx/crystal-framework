package com.lovelycatv.crystalframework.user.service.manager.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.user.entity.UserBanRecordEntity
import com.lovelycatv.crystalframework.user.repository.UserBanRecordRepository
import com.lovelycatv.crystalframework.user.service.manager.UserBanRecordManagerService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class UserBanRecordManagerServiceImpl(
    private val userBanRecordRepository: UserBanRecordRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : UserBanRecordManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, UserBanRecordEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<UserBanRecordEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<UserBanRecordEntity> = UserBanRecordEntity::class

    override fun getRepository(): UserBanRecordRepository = userBanRecordRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun banUser(userId: Long, reason: String, banUntil: Long?, operatorUserId: Long) {
        this.getRepository().save(
            UserBanRecordEntity(
                id = snowIdGenerator.nextId(),
                userId = userId,
                reason = reason,
                banUntil = banUntil,
                liftedTime = null,
                operatorUserId = operatorUserId,
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create ban record")
    }

    override suspend fun unbanUser(userId: Long) {
        val activeBan = this.getActiveBan(userId) ?: throw BusinessException("User $userId is not currently banned")
        this.withUpdateEntityContext(activeBan.id) {
            this.getRepository().save(
                activeBan.apply {
                    this.liftedTime = System.currentTimeMillis()
                    onUpdate()
                } newEntity false
            ).awaitFirstOrNull() ?: throw BusinessException("Could not lift the ban of user $userId")
        }
    }

    override suspend fun getActiveBan(userId: Long): UserBanRecordEntity? {
        val now = System.currentTimeMillis()
        return this.getRepository()
            .findByUserId(userId)
            .awaitListWithTimeout()
            .firstOrNull { it.isEffective(now) }
    }

    override suspend fun getActivelyBannedUserIds(userIds: Collection<Long>): Set<Long> {
        if (userIds.isEmpty()) {
            return emptySet()
        }
        val now = System.currentTimeMillis()
        return this.getRepository()
            .findByUserIdIn(userIds)
            .awaitListWithTimeout()
            .filter { it.isEffective(now) }
            .map { it.userId }
            .toSet()
    }

    /**
     * A ban record is effective at [now] when it has not been lifted and either never expires or
     * has not expired yet. Shared by [getActiveBan] and [getActivelyBannedUserIds].
     */
    private fun UserBanRecordEntity.isEffective(now: Long): Boolean =
        this.liftedTime == null && (this.banUntil == null || this.banUntil!! > now)
}
