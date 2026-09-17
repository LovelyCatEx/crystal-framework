/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.controller.manager.currency

import com.lovelycatv.crystalframework.economy.constants.EconomyPermission
import com.lovelycatv.crystalframework.economy.controller.manager.currency.dto.ManagerCreateCurrencyDTO
import com.lovelycatv.crystalframework.economy.controller.manager.currency.dto.ManagerDeleteCurrencyDTO
import com.lovelycatv.crystalframework.economy.controller.manager.currency.dto.ManagerReadCurrencyDTO
import com.lovelycatv.crystalframework.economy.controller.manager.currency.dto.ManagerUpdateCurrencyDTO
import com.lovelycatv.crystalframework.economy.entity.CurrencyEntity
import com.lovelycatv.crystalframework.economy.repository.CurrencyRepository
import com.lovelycatv.crystalframework.economy.service.manager.CurrencyManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/currency")
class CurrencyManagerController(
    managerService: CurrencyManagerService,
) : StandardManagerController<
    CurrencyManagerService,
    CurrencyRepository,
    CurrencyEntity,
    ManagerCreateCurrencyDTO,
    ManagerReadCurrencyDTO,
    ManagerUpdateCurrencyDTO,
    ManagerDeleteCurrencyDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = EconomyPermission.ACTION_SYSTEM_ECONOMY_CURRENCY_CREATE_NAME,
        systemRead = EconomyPermission.ACTION_SYSTEM_ECONOMY_CURRENCY_READ_NAME,
        systemUpdate = EconomyPermission.ACTION_SYSTEM_ECONOMY_CURRENCY_UPDATE_NAME,
        systemDelete = EconomyPermission.ACTION_SYSTEM_ECONOMY_CURRENCY_DELETE_NAME,
    ),
)
