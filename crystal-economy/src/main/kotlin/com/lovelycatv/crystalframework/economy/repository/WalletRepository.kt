/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.repository

import com.lovelycatv.crystalframework.economy.entity.WalletEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface WalletRepository : BaseRepository<WalletEntity> {
    fun findByScopeAndScopeIdAndOwnerIdAndCurrencyId(
        scope: Int,
        scopeId: Long,
        ownerId: Long,
        currencyId: Long,
    ): Mono<WalletEntity>

    fun findAllByScopeId(scopeId: Long): Flux<WalletEntity>
}
