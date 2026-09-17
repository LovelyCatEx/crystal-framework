/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.service.impl

import com.lovelycatv.crystalframework.economy.entity.EconomyTransactionEntity
import com.lovelycatv.crystalframework.economy.entity.WalletEntity
import com.lovelycatv.crystalframework.economy.repository.CurrencyRepository
import com.lovelycatv.crystalframework.economy.repository.EconomyTransactionRepository
import com.lovelycatv.crystalframework.economy.repository.WalletRepository
import com.lovelycatv.crystalframework.economy.service.EconomyWalletService
import com.lovelycatv.crystalframework.economy.types.EconomyChargeResult
import com.lovelycatv.crystalframework.economy.types.EconomyTransactionType
import com.lovelycatv.crystalframework.shared.constants.TableConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.lockRowForUpdate
import com.lovelycatv.crystalframework.shared.utils.reactor.withDistributedTransactionName
import com.lovelycatv.crystalframework.sdk.economy.TenantMemberIdResolver
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.annotation.Lazy
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class EconomyWalletServiceImpl(
    private val walletRepository: WalletRepository,
    private val currencyRepository: CurrencyRepository,
    private val transactionRepository: EconomyTransactionRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val transactionalOperator: TransactionalOperator,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
    @Lazy private val tenantMemberIdResolver: TenantMemberIdResolver?,
) : EconomyWalletService {

    private class InsufficientBalanceException : RuntimeException()

    override suspend fun getBalance(scope: ResourceScope, scopeId: Long, ownerId: Long, currencyId: Long): BigDecimal {
        val units = walletRepository.findByScopeAndScopeIdAndOwnerIdAndCurrencyId(scope.typeId, scopeId, ownerId, currencyId)
            .awaitFirstOrNull()?.balance ?: 0L
        val currency = currencyRepository.findById(currencyId).awaitFirstOrNull()
            ?: return BigDecimal.ZERO
        return fromUnits(units, currency.precision)
    }

    override suspend fun adjust(
        scope: ResourceScope,
        scopeId: Long,
        ownerId: Long,
        currencyId: Long,
        signedAmount: BigDecimal,
        type: Int,
        requestId: String,
        referenceType: Int,
        referenceId: Long?,
        remark: String?,
    ): EconomyTransactionEntity {
        val currency = currencyRepository.findById(currencyId).awaitFirstOrNull()
            ?: throw BusinessException("Currency not found: $currencyId")
        val units = toUnits(signedAmount, currency.precision)

        transactionRepository.findByRequestId(requestId).awaitFirstOrNull()?.let { return it }

        val wallet = getOrCreateWallet(scope, scopeId, ownerId, currencyId)

        return withDistributedTransactionName("AdjustWallet") {
            transactionalOperator.executeAndAwait {
                r2dbcEntityTemplate.lockRowForUpdate(TableConstants.TABLE_ECONOMY_WALLETS, wallet.id)
                val current = walletRepository.findById(wallet.id).awaitFirstOrNull()
                    ?: throw BusinessException("Wallet not found: ${wallet.id}")

                val before = current.balance
                val after = before + units
                if (units < 0 && after < 0) {
                    throw BusinessException("Insufficient balance: required ${-units}, available $before")
                }

                current.balance = after
                current.onUpdate()
                walletRepository.save(current).awaitFirstOrNull()

                val transaction = EconomyTransactionEntity(
                    id = snowIdGenerator.nextId(),
                    scope = scope.typeId,
                    scopeId = scopeId,
                    ownerId = ownerId,
                    requestId = requestId,
                    type = type,
                    currencyId = currencyId,
                    amount = units,
                    balanceBefore = before,
                    balanceAfter = after,
                    referenceType = referenceType,
                    referenceId = referenceId,
                    remark = remark,
                ).apply { newEntity() }
                transactionRepository.save(transaction).awaitFirstOrNull()
                    ?: throw BusinessException("Could not create economy transaction")
            }
        }
    }

    override suspend fun charge(
        userId: Long,
        tenantId: Long?,
        currencyId: Long,
        amount: BigDecimal,
        referenceType: Int,
        referenceId: Long?,
        requestId: String,
    ): EconomyChargeResult {
        if (amount.signum() <= 0) return EconomyChargeResult.CHARGED

        val currency = currencyRepository.findById(currencyId).awaitFirstOrNull()
            ?: return EconomyChargeResult.CURRENCY_NOT_FOUND
        val units = toUnits(amount, currency.precision)
        if (units <= 0) return EconomyChargeResult.CHARGED

        transactionRepository.findByRequestId("$requestId:tenant").awaitFirstOrNull()
            ?.let { return EconomyChargeResult.CHARGED }
        transactionRepository.findByRequestId("$requestId:user").awaitFirstOrNull()
            ?.let { return EconomyChargeResult.CHARGED }

        return try {
            withDistributedTransactionName("ChargeWallet") {
                transactionalOperator.executeAndAwait {
                    var remaining = units
                    if (tenantId != null) {
                        val memberId = tenantMemberIdResolver?.resolveMemberId(tenantId, userId)
                        if (memberId != null) {
                            val tenantWallet = getOrCreateWallet(ResourceScope.TENANT, tenantId, memberId, currency.id)
                            remaining = deductFromWallet(tenantWallet, currency.id, remaining, referenceType, referenceId, "$requestId:${ResourceScope.TENANT.name.lowercase()}")
                        }
                    }
                    if (remaining > 0) {
                        val userWallet = getOrCreateWallet(ResourceScope.SYSTEM, 0, userId, currency.id)
                        remaining = deductFromWallet(userWallet, currency.id, remaining, referenceType, referenceId, "$requestId:${ResourceScope.SYSTEM.name.lowercase()}")
                    }
                    if (remaining > 0) {
                        throw InsufficientBalanceException()
                    }
                    EconomyChargeResult.CHARGED
                }
            }
        } catch (_: InsufficientBalanceException) {
            EconomyChargeResult.INSUFFICIENT_BALANCE
        }
    }

    private suspend fun getOrCreateWallet(
        scope: ResourceScope,
        scopeId: Long,
        ownerId: Long,
        currencyId: Long,
    ): WalletEntity {
        walletRepository.findByScopeAndScopeIdAndOwnerIdAndCurrencyId(scope.typeId, scopeId, ownerId, currencyId)
            .awaitFirstOrNull()?.let { return it }

        val wallet = WalletEntity(
            id = snowIdGenerator.nextId(),
            scope = scope.typeId,
            scopeId = scopeId,
            ownerId = ownerId,
            currencyId = currencyId,
            balance = 0L,
        ).apply { newEntity() }

        return try {
            walletRepository.save(wallet).awaitFirstOrNull()
                ?: throw BusinessException("Could not create wallet")
        } catch (_: DataIntegrityViolationException) {
            walletRepository.findByScopeAndScopeIdAndOwnerIdAndCurrencyId(scope.typeId, scopeId, ownerId, currencyId)
                .awaitFirstOrNull()
                ?: throw BusinessException("Could not create wallet")
        }
    }

    private suspend fun deductFromWallet(
        wallet: WalletEntity,
        currencyId: Long,
        amount: Long,
        referenceType: Int,
        referenceId: Long?,
        requestId: String,
    ): Long {
        r2dbcEntityTemplate.lockRowForUpdate(TableConstants.TABLE_ECONOMY_WALLETS, wallet.id)
        val current = walletRepository.findById(wallet.id).awaitFirstOrNull()
            ?: throw BusinessException("Wallet not found: ${wallet.id}")

        val available = current.balance
        if (available <= 0) return amount

        val toDeduct = minOf(amount, available)
        val after = available - toDeduct

        current.balance = after
        current.onUpdate()
        walletRepository.save(current).awaitFirstOrNull()

        val transaction = EconomyTransactionEntity(
            id = snowIdGenerator.nextId(),
            scope = current.scope,
            scopeId = current.scopeId,
            ownerId = current.ownerId,
            requestId = requestId,
            type = EconomyTransactionType.DEDUCT.typeId,
            currencyId = currencyId,
            amount = -toDeduct,
            balanceBefore = available,
            balanceAfter = after,
            referenceType = referenceType,
            referenceId = referenceId,
        ).apply { newEntity() }
        transactionRepository.save(transaction).awaitFirstOrNull()
            ?: throw BusinessException("Could not create economy transaction")

        return amount - toDeduct
    }

    // Balances are stored as integers scaled by 10^(precision + 2): the 2 extra decimal places
    // beyond the display precision are rounding headroom for fractional charges.
    private fun toUnits(amount: BigDecimal, precision: Int): Long =
        amount.movePointRight(precision + 2).setScale(0, RoundingMode.HALF_UP).longValueExact()

    private fun fromUnits(units: Long, precision: Int): BigDecimal =
        BigDecimal.valueOf(units, precision + 2)
}
