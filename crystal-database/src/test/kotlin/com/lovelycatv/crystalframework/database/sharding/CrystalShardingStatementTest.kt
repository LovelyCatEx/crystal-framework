package com.lovelycatv.crystalframework.database.sharding

import com.lovelycatv.crystalframework.database.CrystalShardingConnectionFactory
import com.lovelycatv.crystalframework.database.R2dbcConnectionPoolRegistry
import com.lovelycatv.crystalframework.database.R2dbcDataSourceDeclaration
import com.lovelycatv.crystalframework.database.R2dbcDataSourceRegistry
import com.lovelycatv.crystalframework.database.exception.ShardingException
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.Connection
import io.r2dbc.spi.Statement
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Tests Crystal Framework sharding architecture ([com.lovelycatv.crystalframework.database.CrystalShardingConnectionFactory] / [com.lovelycatv.crystalframework.database.CrystalShardingConnection] /
 * [com.lovelycatv.crystalframework.database.CrystalShardingStatement]) core routing logic: literal immediate routing, placeholder deferred routing,
 * batch single-table constraint.
 */
class CrystalShardingStatementTest {

    // Simple mod-4 algorithm: users -> primary.users_<value % 4>
    private val modAlgorithm = object : ShardingAlgorithm {
        override fun doSharding(logicalTable: String, shardingValue: Any?): ShardingTarget {
            val n = (shardingValue as Number).toLong()
            return ShardingTarget(
                dataSourceName = "primary",
                tableName = "${logicalTable}_${n % 4}"
            )
        }
    }

    private val actualTables = listOf("users_0", "users_1", "users_2", "users_3")

    private fun createRegistry(rule: R2dbcShardingRule): R2dbcShardingRuleRegistry {
        val dataSources = R2dbcDataSourceRegistry(
            listOf(R2dbcDataSourceDeclaration("primary", "r2dbc:postgresql://localhost/db", "u", "p")),
        )
        return R2dbcShardingRuleRegistry(
            listOf(
                object : R2dbcShardingRuleComponent {
                    override val name = "users-sharding"
                    override fun rules() = listOf(rule)
                },
            ),
            dataSources,
        )
    }

    private fun shardedRule() = R2dbcShardingRule(
        tableName = "users",
        dataSourceName = "primary",
        shardingColumn = "tenant_id",
        actualTables = actualTables,
        algorithm = modAlgorithm,
    )

    /** Mock connection-pool registry that returns a SQL-recording mock connection */
    private fun createMockPoolRegistry(recordingConnection: RecordingConnection): R2dbcConnectionPoolRegistry {
        val mockPool = mock(ConnectionPool::class.java).apply {
            `when`(create()).thenReturn(Mono.just(recordingConnection.connection))
        }
        return R2dbcConnectionPoolRegistry(mockPool, emptyList())
    }

    /** Mock connection that records the SQL passed to createStatement */
    private class RecordingConnection {
        val connection: Connection = mock(Connection::class.java)
        val sqlCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val statement: Statement = mock(Statement::class.java)

        init {
            `when`(connection.createStatement(sqlCaptor.capture())).thenReturn(statement)
            `when`(connection.close()).thenReturn(Mono.empty<Void>())
            `when`(statement.bind(any<Int>(), any())).thenReturn(statement)
            `when`(statement.bind(any<String>(), any())).thenReturn(statement)
            `when`(statement.add()).thenReturn(statement)
            `when`(statement.execute()).thenReturn(Flux.empty())
        }

        fun lastSql(): String = sqlCaptor.value
    }

    private lateinit var connectionFactory: CrystalShardingConnectionFactory

    @BeforeEach
    fun setUp() {
        // Each test creates its own factory (needs a distinct mock connection to verify SQL)
    }

    @AfterEach
    fun tearDown() {
        // No-op
    }

    @Test
    fun `literal sharding value rewrites table immediately`() {
        val rec = RecordingConnection()
        val poolRegistry = createMockPoolRegistry(rec)
        connectionFactory = CrystalShardingConnectionFactory(poolRegistry, createRegistry(shardedRule()))

        val connection = Mono.from(connectionFactory.create()).block()!!
        val stmt = connection.createStatement("INSERT INTO users (id, tenant_id, name) VALUES (1, 6, 'Alice')")
        Mono.from(stmt.execute()).block()

        // 6 % 4 = 2 -> users_2 ; literal sharding value resolved at execute time
        assertTrue(rec.lastSql().contains("users_2"), "SQL was: ${rec.lastSql()}")
    }

    @Test
    fun `deferred placeholder rewrites at execute using bound value`() {
        val rec = RecordingConnection()
        val poolRegistry = createMockPoolRegistry(rec)
        connectionFactory = CrystalShardingConnectionFactory(poolRegistry, createRegistry(shardedRule()))

        val connection = Mono.from(connectionFactory.create()).block()!!
        val stmt = connection.createStatement("INSERT INTO users (id, tenant_id, name) VALUES (\$1, \$2, \$3)")

        // bind $2 -> index 1 = 9 ; 9 % 4 = 1 -> users_1
        stmt.bind(0, 100L).bind(1, 9L).bind(2, "Bob")
        Mono.from(stmt.execute()).block()

        assertTrue(rec.lastSql().contains("users_1"), "SQL was: ${rec.lastSql()}")
        verify(rec.statement).bind(0, 100L)
        verify(rec.statement).bind(1, 9L)
        verify(rec.statement).bind(2, "Bob")
    }

    @Test
    fun `batch with same actual table is allowed`() {
        val rec = RecordingConnection()
        val poolRegistry = createMockPoolRegistry(rec)
        connectionFactory = CrystalShardingConnectionFactory(poolRegistry, createRegistry(shardedRule()))

        val connection = Mono.from(connectionFactory.create()).block()!!
        val stmt = connection.createStatement("INSERT INTO users (id, tenant_id, name) VALUES (\$1, \$2, \$3)")

        stmt.bind(0, 1L).bind(1, 2L).bind(2, "A") // 2 % 4 = 2 -> users_2
            .add()
            .bind(0, 2L).bind(1, 6L).bind(2, "B") // 6 % 4 = 2 -> users_2
        Mono.from(stmt.execute()).block()

        assertTrue(rec.lastSql().contains("users_2"), "SQL was: ${rec.lastSql()}")
        verify(rec.statement).add()
    }

    @Test
    fun `batch spanning multiple actual tables is rejected`() {
        val rec = RecordingConnection()
        val poolRegistry = createMockPoolRegistry(rec)
        connectionFactory = CrystalShardingConnectionFactory(poolRegistry, createRegistry(shardedRule()))

        val connection = Mono.from(connectionFactory.create()).block()!!
        val stmt = connection.createStatement("INSERT INTO users (id, tenant_id, name) VALUES (\$1, \$2, \$3)")

        stmt.bind(0, 1L).bind(1, 2L).bind(2, "A") // -> users_2
            .add()
            .bind(0, 2L).bind(1, 3L).bind(2, "B") // 3 % 4 = 3 -> users_3

        assertThrows<ShardingException> {
            Mono.from(stmt.execute()).block()
        }
    }

    @Test
    fun `algorithm output outside actualTables is rejected`() {
        val badRule = R2dbcShardingRule(
            tableName = "users",
            dataSourceName = "primary",
            shardingColumn = "tenant_id",
            actualTables = listOf("users_0"), // only 0
            algorithm = modAlgorithm,
        )
        val rec = RecordingConnection()
        val poolRegistry = createMockPoolRegistry(rec)
        connectionFactory = CrystalShardingConnectionFactory(poolRegistry, createRegistry(badRule))

        val connection = Mono.from(connectionFactory.create()).block()!!
        val stmt = connection.createStatement("INSERT INTO users (id, tenant_id, name) VALUES (\$1, \$2, \$3)")

        stmt.bind(0, 1L).bind(1, 6L).bind(2, "X") // 6 % 4 = 2 -> users_2 not in actualTables

        assertThrows<ShardingException> {
            Mono.from(stmt.execute()).block()
        }
    }

    @Test
    fun `unsharded table delegates to primary unchanged`() {
        val rec = RecordingConnection()
        val poolRegistry = createMockPoolRegistry(rec)
        // no sharding rules
        val emptyRegistry = R2dbcShardingRuleRegistry(
            emptyList(),
            R2dbcDataSourceRegistry(
                listOf(R2dbcDataSourceDeclaration("primary", "r2dbc:postgresql://localhost/db", "u", "p")),
            ),
        )
        connectionFactory = CrystalShardingConnectionFactory(poolRegistry, emptyRegistry)

        val connection = Mono.from(connectionFactory.create()).block()!!
        val stmt = connection.createStatement("SELECT * FROM system_settings WHERE id = 1")
        Mono.from(stmt.execute()).block()

        assertTrue(rec.lastSql().contains("system_settings"), "SQL was: ${rec.lastSql()}")
    }
}
