/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.controller.manager.transaction

import com.lovelycatv.crystalframework.economy.constants.EconomyPermission
import com.lovelycatv.crystalframework.economy.constants.EconomyTenantPermission
import com.lovelycatv.crystalframework.economy.controller.manager.transaction.dto.ManagerCreateEconomyTransactionDTO
import com.lovelycatv.crystalframework.economy.controller.manager.transaction.dto.ManagerDeleteEconomyTransactionDTO
import com.lovelycatv.crystalframework.economy.controller.manager.transaction.dto.ManagerReadEconomyTransactionDTO
import com.lovelycatv.crystalframework.economy.controller.manager.transaction.dto.ManagerUpdateEconomyTransactionDTO
import com.lovelycatv.crystalframework.economy.entity.EconomyTransactionEntity
import com.lovelycatv.crystalframework.economy.repository.EconomyTransactionRepository
import com.lovelycatv.crystalframework.economy.service.manager.EconomyTransactionManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.ReadonlyScopedManagerController
import com.lovelycatv.crystalframework.shared.controller.of
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/transaction")
class EconomyTransactionManagerController(
    managerService: EconomyTransactionManagerService,
) : ReadonlyScopedManagerController<
    EconomyTransactionManagerService,
    EconomyTransactionRepository,
    EconomyTransactionEntity,
    ManagerCreateEconomyTransactionDTO,
    ManagerReadEconomyTransactionDTO,
    ManagerUpdateEconomyTransactionDTO,
    ManagerDeleteEconomyTransactionDTO
>(
    managerService,
    permissions = PermissionMatrix.of {
        `super` {
            create = PermissionMatrix.NEVER_GRANTED
            read = EconomyPermission.ACTION_X_ECONOMY_TRANSACTION_READ.name
            update = PermissionMatrix.NEVER_GRANTED
            delete = PermissionMatrix.NEVER_GRANTED
        }
        system {
            create = PermissionMatrix.NEVER_GRANTED
            read = EconomyPermission.ACTION_SYSTEM_ECONOMY_TRANSACTION_READ.name
            update = PermissionMatrix.NEVER_GRANTED
            delete = PermissionMatrix.NEVER_GRANTED
        }
        tenantAdmin {
            create = PermissionMatrix.NEVER_GRANTED
            read = EconomyPermission.ACTION_TENANT_ECONOMY_TRANSACTION_READ.name
            update = PermissionMatrix.NEVER_GRANTED
            delete = PermissionMatrix.NEVER_GRANTED
        }
        tenantPem {
            create = PermissionMatrix.NEVER_GRANTED
            read = EconomyTenantPermission.ACTION_ECONOMY_TRANSACTION_READ.name
            update = PermissionMatrix.NEVER_GRANTED
            delete = PermissionMatrix.NEVER_GRANTED
        }
    },
) {
    override suspend fun buildReadAllResponse(scopeId: Long): Any {
        throw UnsupportedOperationException("read-all is not supported for economy transactions")
    }
}
