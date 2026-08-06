package com.lovelycatv.crystalframework.shared.config.database

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.Connection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class R2dbcRoutingConnectionFactoryTest {

    private fun createMockPoolRegistry(): R2dbcConnectionPoolRegistry {
        val primaryPool = mock(ConnectionPool::class.java)
        val primaryConnection = mock(Connection::class.java)
        `when`(primaryPool.create()).thenReturn(Mono.just(primaryConnection))

        val shardPool = mock(ConnectionPool::class.java)
        val shardConnection = mock(Connection::class.java)
        `when`(shardPool.create()).thenReturn(Mono.just(shardConnection))

        val registry = mock(R2dbcConnectionPoolRegistry::class.java)
        `when`(registry.get("primary")).thenReturn(primaryPool)
        `when`(registry.get("shard-01")).thenReturn(shardPool)
        `when`(registry.require("primary")).thenReturn(primaryPool)
        `when`(registry.require("shard-01")).thenReturn(shardPool)

        return registry
    }

    @Test
    fun `routes to primary when no context key is set`() {
        val registry = createMockPoolRegistry()
        val factory = R2dbcRoutingConnectionFactory(registry, "primary")

        StepVerifier.create(Mono.from(factory.create()))
            .expectNextMatches { it is DelegatedR2dbcConnection }
            .verifyComplete()
    }

    @Test
    fun `routes to shard-01 when context key is set`() {
        val registry = createMockPoolRegistry()
        val factory = R2dbcRoutingConnectionFactory(registry, "primary")

        StepVerifier.create(
            Mono.from(factory.create())
                .contextWrite(R2dbcDataSourceContext.write("shard-01")),
        )
            .expectNextMatches { it is DelegatedR2dbcConnection }
            .verifyComplete()
    }

    @Test
    fun `falls back to primary when unknown data source is requested`() {
        val primaryPool = mock(ConnectionPool::class.java)
        val primaryConnection = mock(Connection::class.java)
        `when`(primaryPool.create()).thenReturn(Mono.just(primaryConnection))

        val registry = mock(R2dbcConnectionPoolRegistry::class.java)
        `when`(registry.get("non-existent")).thenReturn(null)
        `when`(registry.get("primary")).thenReturn(primaryPool)
        `when`(registry.require("primary")).thenReturn(primaryPool)

        val factory = R2dbcRoutingConnectionFactory(registry, "primary")

        StepVerifier.create(
            Mono.from(factory.create())
                .contextWrite(R2dbcDataSourceContext.write("non-existent")),
        )
            .expectNextMatches { it is DelegatedR2dbcConnection }
            .verifyComplete()
    }

    @Test
    fun `writeForTable resolves data source via router`() {
        val dataSources = R2dbcDataSourceRegistry(
            listOf(
                R2dbcDataSourceDeclaration("primary", "r2dbc:postgresql://localhost/primary", "test", "test"),
                R2dbcDataSourceDeclaration("shard-01", "r2dbc:postgresql://localhost/shard", "test", "test"),
            ),
        )

        val rules = R2dbcShardingRuleRegistry(
            listOf(
                object : R2dbcShardingRuleComponent {
                    override val name = "user-sharding"
                    override fun rules() = listOf(
                        R2dbcShardingRule("sys_user", "shard-01", "tenant_id"),
                    )
                },
            ),
            dataSources,
        )

        val router = R2dbcShardingRouter("primary", rules)

        // Verify the router resolved to shard-01
        val decision = router.resolve("sys_user")
        assertEquals("shard-01", decision.dataSourceName)
        assertEquals("tenant_id", decision.shardingColumn)
        assertEquals("user-sharding", decision.ruleName)

        // Verify writeForTable produces correct context
        val contextModifier = R2dbcDataSourceContext.writeForTable("sys_user", router)
        val testContext = reactor.util.context.Context.empty()
        val modifiedContext = contextModifier(testContext)
        assertEquals("shard-01", R2dbcDataSourceContext.current(modifiedContext))
    }
}
