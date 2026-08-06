package com.lovelycatv.crystalframework.shared.config.database

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.Connection
import io.r2dbc.spi.Result
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
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

/**
 * 测试 Crystal Framework 分片架构（[CrystalShardingConnectionFactory] / [CrystalShardingConnection] /
 * [CrystalShardingStatement]）的核心路由逻辑：字面量当场路由、占位符延迟路由、批处理单表约束。
 */
class CrystalShardingStatementTest {

    // 简单 mod-4 算法：users -> users_<value % 4>
    private val modAlgorithm = object : ShardingAlgorithm {
        override fun doSharding(logicalTable: String, shardingValue: Any?): String {
            val n = (shardingValue as Number).toLong()
            return "${logicalTable}_${n % 4}"
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

    /** Mock 连接池注册表，返回能记录 SQL 的 mock 连接 */
    private fun createMockPoolRegistry(recordingConnection: RecordingConnection): R2dbcConnectionPoolRegistry {
        val mockPool = mock(ConnectionPool::class.java).apply {
            `when`(create()).thenReturn(Mono.just(recordingConnection.connection))
        }
        return R2dbcConnectionPoolRegistry(mockPool, emptyList())
    }

    /** Mock 连接，记录 createStatement 传入的 SQL */
    private class RecordingConnection {
        val connection: Connection = mock(Connection::class.java)
        val sqlCaptor: ArgumentCaptor<String> = ArgumentCaptor.forClass(String::class.java)
        val statement: Statement = mock(Statement::class.java)

        init {
            @Suppress("UNCHECKED_CAST")
            val result = mock(Publisher::class.java) as Publisher<Result>
            `when`(connection.createStatement(sqlCaptor.capture())).thenReturn(statement)
            `when`(statement.bind(org.mockito.kotlin.any<Int>(), org.mockito.kotlin.any())).thenReturn(statement)
            `when`(statement.bind(org.mockito.kotlin.any<String>(), org.mockito.kotlin.any())).thenReturn(statement)
            `when`(statement.add()).thenReturn(statement)
            `when`(statement.execute()).thenReturn(result)
        }

        fun lastSql(): String = sqlCaptor.value
    }

    private lateinit var connectionFactory: CrystalShardingConnectionFactory

    @BeforeEach
    fun setUp() {
        // 每个测试自己创建 factory（因为需要不同的 mock 连接来验证 SQL）
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

        // 6 % 4 = 2 -> users_2，createStatement 时已改写（字面量）
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
            actualTables = listOf("users_0"), // 只有 0
            algorithm = modAlgorithm,
        )
        val rec = RecordingConnection()
        val poolRegistry = createMockPoolRegistry(rec)
        connectionFactory = CrystalShardingConnectionFactory(poolRegistry, createRegistry(badRule))

        val connection = Mono.from(connectionFactory.create()).block()!!
        val stmt = connection.createStatement("INSERT INTO users (id, tenant_id, name) VALUES (\$1, \$2, \$3)")

        stmt.bind(0, 1L).bind(1, 6L).bind(2, "X") // 6 % 4 = 2 -> users_2 不在 actualTables

        assertThrows<ShardingException> {
            Mono.from(stmt.execute()).block()
        }
    }

    @Test
    fun `unsharded table delegates to primary unchanged`() {
        val rec = RecordingConnection()
        val poolRegistry = createMockPoolRegistry(rec)
        // 无分片规则
        val emptyRegistry = R2dbcShardingRuleRegistry(
            emptyList(),
            R2dbcDataSourceRegistry(
                listOf(R2dbcDataSourceDeclaration("primary", "r2dbc:postgresql://localhost/db", "u", "p")),
            ),
        )
        connectionFactory = CrystalShardingConnectionFactory(poolRegistry, emptyRegistry)

        val connection = Mono.from(connectionFactory.create()).block()!!
        connection.createStatement("SELECT * FROM system_settings WHERE id = 1")

        assertTrue(rec.lastSql().contains("system_settings"), "SQL was: ${rec.lastSql()}")
    }
}
