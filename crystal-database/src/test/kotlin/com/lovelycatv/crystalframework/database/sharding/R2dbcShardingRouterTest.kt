package com.lovelycatv.crystalframework.database.sharding

import com.lovelycatv.crystalframework.database.R2dbcDataSourceDeclaration
import com.lovelycatv.crystalframework.database.R2dbcDataSourceRegistry
import com.lovelycatv.crystalframework.database.R2dbcRouteDecision
import com.lovelycatv.crystalframework.database.constants.R2dbcDataSourceConstants
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test

class R2dbcShardingRouterTest {
    private val dataSources = R2dbcDataSourceRegistry(
        listOf(
            R2dbcDataSourceDeclaration(
                name = R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME,
                url = "r2dbc:postgresql://localhost:5432/main",
                username = "postgres",
                password = "secret",
            ),
            R2dbcDataSourceDeclaration(
                name = "shard-01",
                url = "r2dbc:postgresql://localhost:5433/shard_01",
                username = "postgres",
                password = "secret",
            ),
        ),
    )

    @Test
    fun `registered table resolves component metadata without changing SQL`() {
        val rules = R2dbcShardingRuleRegistry(
            listOf(
                object : R2dbcShardingRuleComponent {
                    override val name = "tenant-users"
                    override fun rules() = listOf(
                        R2dbcShardingRule("users", "shard-01", "tenant_id"),
                    )
                },
            ),
            dataSources,
        )
        val router = R2dbcShardingRouter(R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME, rules)

        assertEquals(
            R2dbcRouteDecision("users", "tenant_id", "tenant-users", "shard-01"),
            router.resolve(" USERS "),
        )
    }

    @Test
    fun `unregistered table falls back to primary`() {
        val rules = R2dbcShardingRuleRegistry(emptyList(), dataSources)
        val router = R2dbcShardingRouter(R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME, rules)

        assertEquals(
            R2dbcRouteDecision("system_settings", null, null, "primary"),
            router.resolve("system_settings"),
        )
        assertEquals(
            R2dbcRouteDecision(null, null, null, "primary"),
            router.resolve(null),
        )
    }

    @Test
    fun `duplicate data source name fails fast`() {
        assertThrows<IllegalStateException> {
            R2dbcDataSourceRegistry(
                listOf(
                    R2dbcDataSourceDeclaration("shard-01", "r2dbc:postgresql://localhost/a", "u", "p"),
                    R2dbcDataSourceDeclaration(" shard-01 ", "r2dbc:postgresql://localhost/b", "u", "p"),
                ),
            )
        }
    }

    @Test
    fun `duplicate table or unknown data source fails fast`() {
        assertThrows<IllegalArgumentException> {
            R2dbcShardingRuleRegistry(
                listOf(
                    object : R2dbcShardingRuleComponent {
                        override val name = "missing-source"
                        override fun rules() = listOf(R2dbcShardingRule("users", "shard-404"))
                    },
                ),
                dataSources,
            )
        }

        val first = object : R2dbcShardingRuleComponent {
            override val name = "first"
            override fun rules() = listOf(R2dbcShardingRule("users", "shard-01"))
        }
        val second = object : R2dbcShardingRuleComponent {
            override val name = "second"
            override fun rules() = listOf(R2dbcShardingRule(" USERS ", "shard-01"))
        }
        assertThrows<IllegalStateException> {
            R2dbcShardingRuleRegistry(listOf(first, second), dataSources)
        }
    }

    @Test
    fun `routing interceptor returns the same AST`() {
        val rules = R2dbcShardingRuleRegistry(emptyList(), dataSources)
        val router = R2dbcShardingRouter(R2dbcDataSourceConstants.DEFAULT_DATA_SOURCE_NAME, rules)
        val interceptor = R2dbcShardingSqlInterceptor(router, true)
        val statement = CCJSqlParserUtil.parse("SELECT * FROM users")

        assertSame(statement, interceptor.intercept(statement, "users"))
    }
}
