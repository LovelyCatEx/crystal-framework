package com.lovelycatv.crystalframework.shared.config

import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.stereotype.Component

/**
 * Installs the [SqlStatementInterceptor] chain into [CrystalFrameworkSQLModifier] once every
 * singleton exists.
 *
 * The wiring must not sit in [R2dbcSQLInterceptorConfig]: its `databaseClient` bean is a foundational
 * R2DBC bean pulled in before component scanning finishes, so a constructor-injected interceptor list
 * there resolves to empty. [SmartInitializingSingleton.afterSingletonsInstantiated] runs at the end
 * of context refresh — after all interceptors are constructed but before any request-time query — so
 * the chain is complete and in time for the first [CrystalFrameworkSQLModifier.processSql] call.
 */
@Component
class SqlStatementInterceptorRegistrar(
    private val interceptors: List<SqlStatementInterceptor>,
) : SmartInitializingSingleton {

    override fun afterSingletonsInstantiated() {
        CrystalFrameworkSQLModifier.setInterceptors(interceptors)
    }
}
