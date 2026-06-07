package com.lovelycatv.crystalframework.mail.service.impl

import com.lovelycatv.crystalframework.mail.entity.MailTemplateCategoryEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateCategoryRepository
import com.lovelycatv.crystalframework.mail.service.MailTemplateCategoryService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MailTemplateCategoryServiceImpl(
    private val mailTemplateCategoryRepository: MailTemplateCategoryRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : MailTemplateCategoryService {
    override val cacheStore: ReactiveExpiringKVStore<String, MailTemplateCategoryEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<MailTemplateCategoryEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<MailTemplateCategoryEntity> = MailTemplateCategoryEntity::class

    override fun getRepository(): MailTemplateCategoryRepository {
        return this.mailTemplateCategoryRepository
    }
}
