/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.service.manager

import com.lovelycatv.crystalframework.economy.controller.manager.transaction.dto.ManagerCreateEconomyTransactionDTO
import com.lovelycatv.crystalframework.economy.controller.manager.transaction.dto.ManagerDeleteEconomyTransactionDTO
import com.lovelycatv.crystalframework.economy.controller.manager.transaction.dto.ManagerReadEconomyTransactionDTO
import com.lovelycatv.crystalframework.economy.controller.manager.transaction.dto.ManagerUpdateEconomyTransactionDTO
import com.lovelycatv.crystalframework.economy.entity.EconomyTransactionEntity
import com.lovelycatv.crystalframework.economy.repository.EconomyTransactionRepository
import com.lovelycatv.crystalframework.shared.database.criteriaFromQueryNode
import com.lovelycatv.crystalframework.shared.service.BaseScopedManagerService
import org.springframework.data.relational.core.query.Criteria

interface EconomyTransactionManagerService : BaseScopedManagerService<
    EconomyTransactionRepository,
    EconomyTransactionEntity,
    ManagerCreateEconomyTransactionDTO,
    ManagerReadEconomyTransactionDTO,
    ManagerUpdateEconomyTransactionDTO,
    ManagerDeleteEconomyTransactionDTO
>