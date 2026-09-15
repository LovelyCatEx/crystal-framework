/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.service

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.economy.repository.EconomyTransactionRepository
import com.lovelycatv.crystalframework.economy.service.manager.CurrencyManagerServiceTest
import com.lovelycatv.crystalframework.economy.types.EconomyChargeResult
import com.lovelycatv.crystalframework.economy.types.EconomyReferenceType
import com.lovelycatv.crystalframework.economy.types.EconomyTransactionType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.tenant.TenantMemberStatus
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EconomyWalletServiceTest(
    @Autowired private val economyWalletService: EconomyWalletService,
    @Autowired private val transactionRepository: EconomyTransactionRepository,
    @Autowired private val tenantMemberRepository: TenantMemberRepository,
    @Autowired private val snowIdGenerator: SnowIdGenerator,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    private val currencyServiceTest: CurrencyManagerServiceTest by lazy { getTestClassInstance(applicationContext) }

    private suspend fun mockMember(tenantId: Long, userId: Long): TenantMemberEntity {
        return tenantMemberRepository.save(
            TenantMemberEntity(
                id = snowIdGenerator.nextId(),
                tenantId = tenantId,
                memberUserId = userId,
                status = TenantMemberStatus.ACTIVE.ordinal,
            ).apply { newEntity() }
        ).awaitFirstOrNull() ?: error("Failed to create tenant member")
    }

    @Test
    fun creditAndDeduct() {
        withTransactionalRollback("economy-credit-deduct") {
            val currency = currencyServiceTest.mockCurrency(precision = 0)
            val userId = 10001L

            economyWalletService.adjust(ResourceScope.SYSTEM, 0, userId, currency.id, BigDecimal("100"), EconomyTransactionType.RECHARGE.typeId, "req-credit-1")
            assertEquals(0, economyWalletService.getBalance(ResourceScope.SYSTEM, 0, userId, currency.id).compareTo(BigDecimal("100")))

            economyWalletService.adjust(ResourceScope.SYSTEM, 0, userId, currency.id, BigDecimal("-30"), EconomyTransactionType.DEDUCT.typeId, "req-deduct-1")
            assertEquals(0, economyWalletService.getBalance(ResourceScope.SYSTEM, 0, userId, currency.id).compareTo(BigDecimal("70")))
        }
    }

    @Test
    fun deductInsufficientThrowsAndKeepsBalance() {
        withTransactionalRollback("economy-deduct-insufficient") {
            val currency = currencyServiceTest.mockCurrency(precision = 0)
            val userId = 10002L
            economyWalletService.adjust(ResourceScope.SYSTEM, 0, userId, currency.id, BigDecimal("100"), EconomyTransactionType.RECHARGE.typeId, "req-credit-2")

            val result = runCatching {
                economyWalletService.adjust(ResourceScope.SYSTEM, 0, userId, currency.id, BigDecimal("-150"), EconomyTransactionType.DEDUCT.typeId, "req-deduct-2")
            }
            val ex = result.exceptionOrNull()
            assertNotNull(ex)
            assertTrue(ex is BusinessException)
            assertTrue((ex as BusinessException).message!!.contains("Insufficient"))

            assertEquals(0, economyWalletService.getBalance(ResourceScope.SYSTEM, 0, userId, currency.id).compareTo(BigDecimal("100")))
        }
    }

    @Test
    fun adjustIsIdempotentByRequestId() {
        withTransactionalRollback("economy-adjust-idempotent") {
            val currency = currencyServiceTest.mockCurrency(precision = 0)
            val userId = 10003L
            val requestId = "req-idempotent-1"

            economyWalletService.adjust(ResourceScope.SYSTEM, 0, userId, currency.id, BigDecimal("100"), EconomyTransactionType.RECHARGE.typeId, requestId)
            economyWalletService.adjust(ResourceScope.SYSTEM, 0, userId, currency.id, BigDecimal("100"), EconomyTransactionType.RECHARGE.typeId, requestId)

            assertEquals(0, economyWalletService.getBalance(ResourceScope.SYSTEM, 0, userId, currency.id).compareTo(BigDecimal("100")))
            val transactions = transactionRepository.findAll().awaitListWithTimeout().filter { it.requestId == requestId }
            assertEquals(1, transactions.size)
        }
    }

    @Test
    fun chargeFallsBackFromTenantToUser() {
        withTransactionalRollback("economy-charge-fallback") {
            val currency = currencyServiceTest.mockCurrency(precision = 0)
            val userId = 20001L
            val tenantId = 30001L
            val member = mockMember(tenantId, userId)
            economyWalletService.adjust(ResourceScope.TENANT, tenantId, member.id, currency.id, BigDecimal("60"), EconomyTransactionType.RECHARGE.typeId, "req-tenant-credit")
            economyWalletService.adjust(ResourceScope.SYSTEM, 0, userId, currency.id, BigDecimal("100"), EconomyTransactionType.RECHARGE.typeId, "req-user-credit")

            val result = economyWalletService.charge(userId, tenantId, currency.code, BigDecimal("80"), EconomyReferenceType.AI_INVOCATION.typeId, null, "req-charge-1")
            assertEquals(EconomyChargeResult.CHARGED, result)

            assertEquals(0, economyWalletService.getBalance(ResourceScope.TENANT, tenantId, member.id, currency.id).compareTo(BigDecimal("0")))
            assertEquals(0, economyWalletService.getBalance(ResourceScope.SYSTEM, 0, userId, currency.id).compareTo(BigDecimal("80")))
        }
    }

    @Test
    fun chargeInsufficientRollsBackAtomically() {
        withTransactionalRollback("economy-charge-insufficient") {
            val currency = currencyServiceTest.mockCurrency(precision = 0)
            val userId = 20002L
            val tenantId = 30002L
            economyWalletService.adjust(ResourceScope.SYSTEM, 0, userId, currency.id, BigDecimal("50"), EconomyTransactionType.RECHARGE.typeId, "req-user-credit-2")

            val result = economyWalletService.charge(userId, tenantId, currency.code, BigDecimal("80"), EconomyReferenceType.AI_INVOCATION.typeId, null, "req-charge-2")
            assertEquals(EconomyChargeResult.INSUFFICIENT_BALANCE, result)

            assertEquals(0, economyWalletService.getBalance(ResourceScope.SYSTEM, 0, userId, currency.id).compareTo(BigDecimal("50")))
        }
    }

    @Test
    fun chargeUnknownCurrencyReturnsNotFound() {
        withTransactionalRollback("economy-charge-unknown-currency") {
            val result = economyWalletService.charge(20003L, 30003L, "NO_SUCH_CURRENCY", BigDecimal("10"), EconomyReferenceType.AI_INVOCATION.typeId, null, "req-charge-3")
            assertEquals(EconomyChargeResult.CURRENCY_NOT_FOUND, result)
        }
    }

    @Test
    fun chargeIsIdempotentByRequestId() {
        withTransactionalRollback("economy-charge-idempotent") {
            val currency = currencyServiceTest.mockCurrency(precision = 0)
            val userId = 20004L
            val tenantId = 30004L
            economyWalletService.adjust(ResourceScope.SYSTEM, 0, userId, currency.id, BigDecimal("100"), EconomyTransactionType.RECHARGE.typeId, "req-user-credit-3")

            economyWalletService.charge(userId, tenantId, currency.code, BigDecimal("30"), EconomyReferenceType.AI_INVOCATION.typeId, null, "req-charge-4")
            economyWalletService.charge(userId, tenantId, currency.code, BigDecimal("30"), EconomyReferenceType.AI_INVOCATION.typeId, null, "req-charge-4")

            assertEquals(0, economyWalletService.getBalance(ResourceScope.SYSTEM, 0, userId, currency.id).compareTo(BigDecimal("70")))
        }
    }
}
