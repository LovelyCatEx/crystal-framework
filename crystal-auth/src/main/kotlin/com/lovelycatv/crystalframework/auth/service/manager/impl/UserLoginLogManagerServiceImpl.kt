package com.lovelycatv.crystalframework.auth.service.manager.impl

import com.lovelycatv.crystalframework.auth.entity.UserLoginLogEntity
import com.lovelycatv.crystalframework.auth.repository.UserLoginLogRepository
import com.lovelycatv.crystalframework.auth.service.manager.UserLoginLogManagerService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class UserLoginLogManagerServiceImpl(
    private val userLoginLogRepository: UserLoginLogRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : UserLoginLogManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, UserLoginLogEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<UserLoginLogEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<UserLoginLogEntity> = UserLoginLogEntity::class

    override fun getRepository(): UserLoginLogRepository {
        return this.userLoginLogRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate
}