/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.service.manager

import com.lovelycatv.crystalframework.economy.controller.manager.currency.dto.ManagerCreateCurrencyDTO
import com.lovelycatv.crystalframework.economy.controller.manager.currency.dto.ManagerDeleteCurrencyDTO
import com.lovelycatv.crystalframework.economy.controller.manager.currency.dto.ManagerReadCurrencyDTO
import com.lovelycatv.crystalframework.economy.controller.manager.currency.dto.ManagerUpdateCurrencyDTO
import com.lovelycatv.crystalframework.economy.entity.CurrencyEntity
import com.lovelycatv.crystalframework.economy.repository.CurrencyRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface CurrencyManagerService : CachedBaseManagerService<
    CurrencyRepository,
    CurrencyEntity,
    ManagerCreateCurrencyDTO,
    ManagerReadCurrencyDTO,
    ManagerUpdateCurrencyDTO,
    ManagerDeleteCurrencyDTO
>
