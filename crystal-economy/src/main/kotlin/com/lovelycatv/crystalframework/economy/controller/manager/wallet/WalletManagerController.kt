/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.controller.manager.wallet

import com.lovelycatv.crystalframework.economy.constants.EconomyPermission
import com.lovelycatv.crystalframework.economy.constants.EconomyTenantPermission
import com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto.ManagerAdjustWalletDTO
import com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto.ManagerCreateWalletDTO
import com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto.ManagerDeleteWalletDTO
import com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto.ManagerReadWalletDTO
import com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto.ManagerUpdateWalletDTO
import com.lovelycatv.crystalframework.economy.entity.EconomyTransactionEntity
import com.lovelycatv.crystalframework.economy.entity.WalletEntity
import com.lovelycatv.crystalframework.economy.repository.WalletRepository
import com.lovelycatv.crystalframework.economy.service.EconomyWalletService
import com.lovelycatv.crystalframework.economy.service.manager.WalletManagerService
import com.lovelycatv.crystalframework.sdk.economy.EconomyTransactionTypeRegistry
import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.ReadonlyScopedManagerController
import com.lovelycatv.crystalframework.shared.controller.of
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/wallet")
class WalletManagerController(
    managerService: WalletManagerService,
    private val economyWalletService: EconomyWalletService,
    private val economyTransactionTypeRegistry: EconomyTransactionTypeRegistry,
) : ReadonlyScopedManagerController<
    WalletManagerService,
    WalletRepository,
    WalletEntity,
    ManagerCreateWalletDTO,
    ManagerReadWalletDTO,
    ManagerUpdateWalletDTO,
    ManagerDeleteWalletDTO
>(
    managerService,
    permissions = PermissionMatrix.of {
        `super` {
            create = PermissionMatrix.NEVER_GRANTED
            read = EconomyPermission.ACTION_X_ECONOMY_WALLET_READ.name
            update = PermissionMatrix.NEVER_GRANTED
            delete = PermissionMatrix.NEVER_GRANTED
        }
        system {
            create = PermissionMatrix.NEVER_GRANTED
            read = EconomyPermission.ACTION_SYSTEM_ECONOMY_WALLET_READ.name
            update = PermissionMatrix.NEVER_GRANTED
            delete = PermissionMatrix.NEVER_GRANTED
        }
        tenantAdmin {
            create = PermissionMatrix.NEVER_GRANTED
            read = EconomyPermission.ACTION_TENANT_ECONOMY_WALLET_READ.name
            update = PermissionMatrix.NEVER_GRANTED
            delete = PermissionMatrix.NEVER_GRANTED
        }
        tenantPem {
            create = PermissionMatrix.NEVER_GRANTED
            read = EconomyTenantPermission.ACTION_ECONOMY_WALLET_READ.name
            update = PermissionMatrix.NEVER_GRANTED
            delete = PermissionMatrix.NEVER_GRANTED
        }
    },
) {
    override suspend fun buildReadAllResponse(scopeId: Long): Any {
        throw UnsupportedOperationException("read-all is not supported for wallets")
    }

    @PostMapping("/adjust", version = "1")
    @RequiresAuthority(
        anyOf = [EconomyPermission.ACTION_SYSTEM_ECONOMY_WALLET_ADJUST_NAME],
        scope = ResourceScope.SYSTEM
    )
    suspend fun adjust(
        @Valid @RequestBody dto: ManagerAdjustWalletDTO,
    ): ApiResponse<EconomyTransactionEntity> {
        val scope = ResourceScope.getById(dto.scope)
            ?: throw BusinessException("Invalid scope: ${dto.scope}")
        if (economyTransactionTypeRegistry.getByTypeId(dto.type) == null) {
            throw BusinessException("Invalid transaction type: ${dto.type}")
        }

        val transaction = economyWalletService.adjust(
            scope = scope,
            scopeId = dto.scopeId,
            ownerId = dto.ownerId,
            currencyId = dto.currencyId,
            signedAmount = dto.amount,
            type = dto.type,
            requestId = UUID.randomUUID().toString(),
            referenceId = dto.referenceId,
            remark = dto.remark,
        )
        return ApiResponse.success(transaction)
    }
}
