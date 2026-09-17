/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.service.manager.impl

import com.lovelycatv.crystalframework.economy.controller.manager.transaction.dto.ManagerCreateEconomyTransactionDTO
import com.lovelycatv.crystalframework.economy.controller.manager.transaction.dto.ManagerUpdateEconomyTransactionDTO
import com.lovelycatv.crystalframework.economy.entity.EconomyTransactionEntity
import com.lovelycatv.crystalframework.economy.repository.EconomyTransactionRepository
import com.lovelycatv.crystalframework.economy.service.manager.EconomyTransactionManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class EconomyTransactionManagerServiceImpl(
    private val repository: EconomyTransactionRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : EconomyTransactionManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, EconomyTransactionEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<EconomyTransactionEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<EconomyTransactionEntity> = EconomyTransactionEntity::class

    override fun getRepository(): EconomyTransactionRepository = repository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun findAllByScopeId(scopeId: Long): List<EconomyTransactionEntity> {
        return repository.findAllByScopeId(scopeId).awaitListWithTimeout()
    }

    override suspend fun create(dto: ManagerCreateEconomyTransactionDTO): EconomyTransactionEntity {
        throw BusinessException("Economy transactions cannot be created manually")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateEconomyTransactionDTO,
        original: EconomyTransactionEntity,
    ): EconomyTransactionEntity {
        throw BusinessException("Economy transactions cannot be updated")
    }
}
