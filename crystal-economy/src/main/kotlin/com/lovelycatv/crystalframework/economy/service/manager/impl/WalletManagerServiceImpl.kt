/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.service.manager.impl

import com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto.ManagerCreateWalletDTO
import com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto.ManagerUpdateWalletDTO
import com.lovelycatv.crystalframework.economy.entity.WalletEntity
import com.lovelycatv.crystalframework.economy.repository.WalletRepository
import com.lovelycatv.crystalframework.economy.service.manager.WalletManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class WalletManagerServiceImpl(
    private val repository: WalletRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : WalletManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, WalletEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<WalletEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<WalletEntity> = WalletEntity::class

    override fun getRepository(): WalletRepository = repository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun findAllByScopeId(scopeId: Long): List<WalletEntity> {
        return repository.findAllByScopeId(scopeId).awaitListWithTimeout()
    }

    override suspend fun create(dto: ManagerCreateWalletDTO): WalletEntity {
        throw BusinessException("Wallets cannot be created manually")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateWalletDTO,
        original: WalletEntity,
    ): WalletEntity {
        throw BusinessException("Wallet balances cannot be updated manually")
    }
}
