package com.lovelycatv.crystalframework.mail.service.impl

import com.lovelycatv.crystalframework.mail.entity.MailTemplateCategoryEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateCategoryRepository
import com.lovelycatv.crystalframework.mail.service.MailTemplateCategoryService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MailTemplateCategoryServiceImpl(
    private val mailTemplateCategoryRepository: MailTemplateCategoryRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : MailTemplateCategoryService {
    override val cacheStore: ExpiringKVStore<String, MailTemplateCategoryEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<MailTemplateCategoryEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<MailTemplateCategoryEntity> = MailTemplateCategoryEntity::class

    override fun getRepository(): MailTemplateCategoryRepository {
        return this.mailTemplateCategoryRepository
    }
}
