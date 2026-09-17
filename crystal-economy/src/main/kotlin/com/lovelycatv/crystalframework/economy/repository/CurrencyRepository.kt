/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.repository

import com.lovelycatv.crystalframework.economy.entity.CurrencyEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import reactor.core.publisher.Mono

interface CurrencyRepository : BaseRepository<CurrencyEntity> {
    fun findByCode(code: String): Mono<CurrencyEntity>
}
