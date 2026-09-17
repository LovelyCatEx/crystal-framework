/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.service.manager

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.economy.controller.manager.currency.dto.ManagerCreateCurrencyDTO
import com.lovelycatv.crystalframework.economy.entity.CurrencyEntity
import com.lovelycatv.crystalframework.economy.repository.CurrencyRepository
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CurrencyManagerServiceTest(
    @Autowired private val currencyManagerService: CurrencyManagerService,
    @Autowired private val currencyRepository: CurrencyRepository,
) : CrystalFrameworkApplicationTests() {

    suspend fun mockCurrency(code: String = "CUR-${UUID.randomUUID()}", precision: Int = 0): CurrencyEntity {
        return currencyManagerService.create(
            ManagerCreateCurrencyDTO(
                code = code,
                name = "Test Currency",
                symbol = "T",
                precision = precision,
                description = null,
                enabled = true,
                sort = 0,
            )
        )
    }

    @Test
    fun createCurrency() {
        withTransactionalRollback("currency-create") {
            val currency = mockCurrency()
            assertNotNull(currency)
            val reloaded = currencyRepository.findById(currency.id).awaitFirstOrNull()
            assertNotNull(reloaded)
            assertEquals(currency.code, reloaded.code)
        }
    }

    @Test
    fun createDuplicateCodeThrows() {
        withTransactionalRollback("currency-duplicate-code") {
            val currency = mockCurrency()
            val result = runCatching { mockCurrency(code = currency.code) }
            val ex = result.exceptionOrNull()
            assertNotNull(ex)
            assertTrue(ex is BusinessException)
        }
    }
}
