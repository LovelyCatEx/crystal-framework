/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.service.manager

import com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto.ManagerCreateWalletDTO
import com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto.ManagerDeleteWalletDTO
import com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto.ManagerReadWalletDTO
import com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto.ManagerUpdateWalletDTO
import com.lovelycatv.crystalframework.economy.entity.WalletEntity
import com.lovelycatv.crystalframework.economy.repository.WalletRepository
import com.lovelycatv.crystalframework.shared.database.criteriaFromQueryNode
import com.lovelycatv.crystalframework.shared.service.BaseScopedManagerService
import org.springframework.data.relational.core.query.Criteria

interface WalletManagerService : BaseScopedManagerService<
    WalletRepository,
    WalletEntity,
    ManagerCreateWalletDTO,
    ManagerReadWalletDTO,
    ManagerUpdateWalletDTO,
    ManagerDeleteWalletDTO
>