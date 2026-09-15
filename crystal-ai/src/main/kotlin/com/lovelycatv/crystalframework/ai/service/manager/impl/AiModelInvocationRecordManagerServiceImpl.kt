/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.service.manager.impl

import com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto.ManagerCreateAiModelInvocationRecordDTO
import com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto.ManagerUpdateAiModelInvocationRecordDTO
import com.lovelycatv.crystalframework.ai.entity.AiModelInvocationRecordEntity
import com.lovelycatv.crystalframework.ai.repository.AiModelInvocationRecordRepository
import com.lovelycatv.crystalframework.ai.service.manager.AiModelInvocationRecordManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class AiModelInvocationRecordManagerServiceImpl(
    private val repository: AiModelInvocationRecordRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : AiModelInvocationRecordManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, AiModelInvocationRecordEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<AiModelInvocationRecordEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<AiModelInvocationRecordEntity> = AiModelInvocationRecordEntity::class

    override fun getRepository(): AiModelInvocationRecordRepository = repository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateAiModelInvocationRecordDTO): AiModelInvocationRecordEntity {
        throw BusinessException("AI model invocation records cannot be created manually")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateAiModelInvocationRecordDTO, original: AiModelInvocationRecordEntity): AiModelInvocationRecordEntity {
        throw BusinessException("AI model invocation records cannot be updated")
    }
}
