/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.service

import com.lovelycatv.crystalframework.economy.entity.EconomyTransactionEntity
import com.lovelycatv.crystalframework.economy.types.EconomyChargeResult
import com.lovelycatv.crystalframework.economy.types.EconomyReferenceType
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import java.math.BigDecimal

interface EconomyWalletService {

    suspend fun getBalance(scope: ResourceScope, scopeId: Long, ownerId: Long, currencyId: Long): BigDecimal

    suspend fun adjust(
        scope: ResourceScope,
        scopeId: Long,
        ownerId: Long,
        currencyId: Long,
        signedAmount: BigDecimal,
        type: Int,
        requestId: String,
        referenceType: Int = EconomyReferenceType.NONE.typeId,
        referenceId: Long? = null,
        remark: String? = null,
    ): EconomyTransactionEntity

    suspend fun charge(
        userId: Long,
        tenantId: Long?,
        currencyCode: String,
        amount: BigDecimal,
        referenceType: Int,
        referenceId: Long?,
        requestId: String,
    ): EconomyChargeResult
}
