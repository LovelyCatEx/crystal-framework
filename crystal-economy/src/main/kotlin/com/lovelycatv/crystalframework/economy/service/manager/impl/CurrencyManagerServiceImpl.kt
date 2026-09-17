/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.service.manager.impl

import com.lovelycatv.crystalframework.economy.controller.manager.currency.dto.ManagerCreateCurrencyDTO
import com.lovelycatv.crystalframework.economy.controller.manager.currency.dto.ManagerUpdateCurrencyDTO
import com.lovelycatv.crystalframework.economy.entity.CurrencyEntity
import com.lovelycatv.crystalframework.economy.repository.CurrencyRepository
import com.lovelycatv.crystalframework.economy.service.manager.CurrencyManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class CurrencyManagerServiceImpl(
    private val repository: CurrencyRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : CurrencyManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, CurrencyEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<CurrencyEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<CurrencyEntity> = CurrencyEntity::class

    override fun getRepository(): CurrencyRepository = repository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateCurrencyDTO): CurrencyEntity {
        repository.findByCode(dto.code).awaitFirstOrNull()?.let {
            throw BusinessException("Currency code '${dto.code}' is already taken")
        }
        val entity = CurrencyEntity(
            id = snowIdGenerator.nextId(),
            code = dto.code,
            name = dto.name,
            symbol = dto.symbol,
            precision = dto.precision,
            symbolPosition = dto.symbolPosition,
            decimalSeparator = dto.decimalSeparator,
            thousandsSeparator = dto.thousandsSeparator,
            description = dto.description,
            enabled = dto.enabled,
            sort = dto.sort,
        )
        return withInvalidateEntityCacheContext(entity.id) {
            repository.save(entity newEntity true).awaitFirstOrNull()
                ?: throw BusinessException("Could not create currency")
        }
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateCurrencyDTO,
        original: CurrencyEntity,
    ): CurrencyEntity {
        if (dto.code != null && dto.code != original.code) {
            repository.findByCode(dto.code).awaitFirstOrNull()?.let {
                throw BusinessException("Currency code '${dto.code}' is already taken")
            }
        }
        return original.apply {
            dto.code?.let { code = it }
            dto.name?.let { name = it }
            dto.symbol?.let { symbol = it }
            dto.precision?.let { precision = it }
            dto.symbolPosition?.let { symbolPosition = it }
            dto.decimalSeparator?.let { decimalSeparator = it }
            dto.thousandsSeparator?.let { thousandsSeparator = it }
            dto.description?.let { description = it }
            dto.enabled?.let { enabled = it }
            dto.sort?.let { sort = it }
        }
    }
}
