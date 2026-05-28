package com.lovelycatv.crystalframework.monitor.service.impl

import com.lovelycatv.crystalframework.monitor.controller.vo.*
import com.lovelycatv.crystalframework.monitor.service.DashboardService
import com.lovelycatv.crystalframework.monitor.service.DashboardStatsProvider
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import java.io.File
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.time.Duration
import java.time.Instant
import kotlin.math.round

@Service
class DashboardServiceImpl(
    private val statsProvider: DashboardStatsProvider,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>,
    private val databaseClient: DatabaseClient,
    private val meterRegistry: MeterRegistry,
) : DashboardService {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var serverNameCache: Pair<String, Long>? = null
    private val serverNameCacheTTL = 10 * 60 * 1000L

    override suspend fun getDashboardStats(timeRange: String): DashboardStatsVO {
        val businessStats = getBusinessStats(timeRange)
        val systemMetrics = getSystemMetrics()

        return DashboardStatsVO(
            businessStats = businessStats,
            systemMetrics = systemMetrics
        )
    }

    override suspend fun getBusinessStats(timeRange: String): BusinessStatsVO {
        val (currentStartTime, previousStartTime) = calculateTimeRange(timeRange)
        val now = System.currentTimeMillis()

        val totalUsers = coroutineScope.async {
            calculateStatItem(
                currentCount = statsProvider.countUsers(currentStartTime, now).awaitSingleOrNull() ?: 0,
                previousCount = statsProvider.countUsers(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
            )
        }

        val totalTenants = coroutineScope.async {
            calculateStatItem(
                currentCount = statsProvider.countTenants(currentStartTime, now).awaitSingleOrNull() ?: 0,
                previousCount = statsProvider.countTenants(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
            )
        }

        val totalTenantMembers = coroutineScope.async {
            calculateStatItem(
                currentCount = statsProvider.countTenantMembers(currentStartTime, now).awaitSingleOrNull() ?: 0,
                previousCount = statsProvider.countTenantMembers(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
            )
        }

        val totalFileResources = coroutineScope.async {
            calculateStatItem(
                currentCount = statsProvider.countFileResources(currentStartTime, now).awaitSingleOrNull() ?: 0,
                previousCount = statsProvider.countFileResources(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
            )
        }

        val totalInvitations = coroutineScope.async {
            calculateStatItem(
                currentCount = statsProvider.countTenantInvitations(currentStartTime, now).awaitSingleOrNull() ?: 0,
                previousCount = statsProvider.countTenantInvitations(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
            )
        }

        val totalInvitationRecords = coroutineScope.async {
            calculateStatItem(
                currentCount = statsProvider.countTenantInvitationRecords(currentStartTime, now).awaitSingleOrNull() ?: 0,
                previousCount = statsProvider.countTenantInvitationRecords(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
            )
        }

        val totalOAuthAccounts = coroutineScope.async {
            calculateStatItem(
                currentCount = statsProvider.countOAuthAccounts(currentStartTime, now).awaitSingleOrNull() ?: 0,
                previousCount = statsProvider.countOAuthAccounts(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
            )
        }

        val totalMailSent = coroutineScope.async {
            calculateStatItem(
                currentCount = statsProvider.countMailSent(currentStartTime, now).awaitSingleOrNull() ?: 0,
                previousCount = statsProvider.countMailSent(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
            )
        }

        return BusinessStatsVO(
            totalUsers = totalUsers.await(),
            totalTenants = totalTenants.await(),
            totalTenantMembers = totalTenantMembers.await(),
            totalFileResources = totalFileResources.await(),
            totalMailSent = totalMailSent.await(),
            totalInvitations = totalInvitations.await(),
            totalInvitationRecords = totalInvitationRecords.await(),
            totalOAuthAccounts = totalOAuthAccounts.await(),
        )
    }

    override suspend fun getSystemMetrics(): SystemMetricsVO {
        val osBean = ManagementFactory.getOperatingSystemMXBean()
                as? com.sun.management.OperatingSystemMXBean
            ?: throw BusinessException("${ManagementFactory.getOperatingSystemMXBean()::class.qualifiedName} is not an valid operating system mx bean")
        val memoryMXBean = ManagementFactory.getMemoryMXBean()
        val gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans()

        val systemTotalMemory = osBean.totalMemorySize
        val systemFreeMemory = osBean.freeMemorySize
        val systemUsedMemory = systemTotalMemory - systemFreeMemory

        val heapMemory = memoryMXBean.heapMemoryUsage
        val nonHeapMemory = memoryMXBean.nonHeapMemoryUsage

        val totalGcCount = gcMXBeans.sumOf { it.collectionCount }
        val totalGcTime = gcMXBeans.sumOf { it.collectionTime }
        val avgGcTime = if (totalGcCount > 0) totalGcTime / totalGcCount else 0

        val uptime = ManagementFactory.getRuntimeMXBean().uptime

        val serverName = withContext(Dispatchers.IO) {
            if (serverNameCache != null && System.currentTimeMillis() - serverNameCache!!.second < serverNameCacheTTL) {
                serverNameCache!!.first
            } else {
                val name = System.getenv("HOSTNAME")
                    ?: System.getenv("COMPUTER_NAME")
                    ?: InetAddress.getLocalHost().canonicalHostName
                    ?: InetAddress.getLocalHost().hostName
                    ?: "unknown"

                serverNameCache = name to System.currentTimeMillis()

                name
            }
        }

        val dbActiveConnections = try {
            meterRegistry.get("r2dbc.pool.acquired")
                .gauge()
                .value()
                .toInt()
        } catch (e: Exception) {
            0
        }

        val dbMaxConnections = try {
            meterRegistry.get("r2dbc.pool.max.allocated")
                .gauge()
                .value()
                .toInt()
        } catch (e: Exception) {
            -1
        }

        val cpuLoad = osBean.cpuLoad

        return SystemMetricsVO(
            cpuUsage = MetricItem(
                used = cpuLoad * 100.0,
                total = osBean.availableProcessors,
                usage = cpuLoad * 100.0,
            ),
            memoryUsage = MetricItem(
                used = systemUsedMemory,
                total = systemTotalMemory,
                usage = (systemUsedMemory.toDouble() / systemTotalMemory) * 100
            ),
            jvmHeapMemory = MetricItem(
                used = heapMemory.used,
                total = heapMemory.max,
                usage = 100.0 * heapMemory.used.toDouble() / heapMemory.max
            ),
            jvmNonHeapMemory = MetricItem(
                used = nonHeapMemory.used,
                total = nonHeapMemory.committed,
                usage = if (nonHeapMemory.committed > 0)
                    100.0 * nonHeapMemory.used.toDouble() / nonHeapMemory.committed
                else 0.0
            ),
            dbConnections = MetricItem(
                used = dbActiveConnections,
                total = dbMaxConnections,
                usage = 100.0 * dbActiveConnections / dbMaxConnections,
            ),
            systemLoad = MetricItem(
                used = round(100.0 * osBean.systemLoadAverage) / 100.0,
                total = osBean.availableProcessors,
                usage = with(100.0 * osBean.systemLoadAverage / osBean.availableProcessors) {
                    if (this > 100) {
                        100.0
                    } else {
                        this
                    }
                },
            ),
            diskUsage = run {
                val rootPath = File(".").canonicalFile.toPath().root.toFile()
                val totalSpace = rootPath.totalSpace
                val usableSpace = rootPath.usableSpace
                val usedSpace = totalSpace - usableSpace
                val usagePercent = if (totalSpace > 0)
                    100.0 * (usedSpace.toDouble() / totalSpace)
                else
                    0.0

                MetricItem(
                    used = usedSpace,
                    total = totalSpace,
                    usage = usagePercent
                )
            },
            gcMetrics = GCMetricsItem(
                avgTime = avgGcTime,
                totalTime = totalGcTime,
                count = totalGcCount
            ),
            serverInfo = ServerInfo(
                serverName = serverName,
                databaseVersion = getDatabaseVersion(),
                redisVersion = getRedisVersion(),
                projectVersion = GlobalConstants.APP_VERSION,
                uptime = formatUptime(uptime)
            )
        )
    }

    private fun calculateTimeRange(timeRange: String): Pair<Long, Long> {
        val now = Instant.now()
        val duration = when (timeRange) {
            "1d" -> Duration.ofDays(1)
            "3d" -> Duration.ofDays(3)
            "5d" -> Duration.ofDays(5)
            "1w" -> Duration.ofDays(7)
            "2w" -> Duration.ofDays(14)
            "1m" -> Duration.ofDays(30)
            "3m" -> Duration.ofDays(90)
            "6m" -> Duration.ofDays(180)
            "1y" -> Duration.ofDays(365)
            else -> Duration.ofDays(30)
        }

        val currentStartTime = now.minus(duration).toEpochMilli()
        val previousStartTime = now.minus(duration.multipliedBy(2)).toEpochMilli()

        return Pair(currentStartTime, previousStartTime)
    }

    private fun calculateStatItem(currentCount: Long, previousCount: Long): BusinessStatsVO.StatItem {
        val change = currentCount - previousCount
        val changePercent = if (previousCount > 0) {
            (change.toDouble() / previousCount) * 100
        } else {
            if (currentCount > 0) 100.0 else 0.0
        }

        return BusinessStatsVO.StatItem(
            value = currentCount,
            change = change,
            changePercent = round(changePercent * 100) / 100
        )
    }

    private fun formatUptime(millis: Long): String {
        val days = millis / (1000 * 60 * 60 * 24)
        val hours = (millis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        return "${days}d ${hours}h ${minutes}m"
    }

    suspend fun getDatabaseVersion(): String {
        val unknownDatabaseStr = "Unknown Database"

        return try {
            val metadata = databaseClient.connectionFactory.metadata
            val databaseName = metadata.name.lowercase()

            val versionStr = when {
                databaseName.contains("postgresql") -> {
                    val result = databaseClient.sql("SELECT version() as version")
                        .fetch()
                        .first()
                        .awaitFirstOrNull()
                    val fullVersion = result?.get("version") as? String ?: "Unknown"
                    val match = Regex("PostgreSQL (\\d+\\.\\d+)").find(fullVersion)
                    match?.groupValues?.get(0) ?: "PostgreSQL $fullVersion"
                }
                databaseName.contains("mysql") -> {
                    val result = databaseClient.sql("SELECT VERSION() as version")
                        .fetch()
                        .first()
                        .awaitFirstOrNull()
                    val fullVersion = result?.get("version") as? String ?: "Unknown"
                    "MySQL $fullVersion"
                }
                else -> {
                    try {
                        val result = databaseClient.sql("SELECT version() as version")
                            .fetch()
                            .first()
                            .awaitFirstOrNull()
                        result?.get("version") as? String ?: unknownDatabaseStr
                    } catch (_: Exception) {
                        unknownDatabaseStr
                    }
                }
            }

            versionStr
        } catch (_: Exception) {
            unknownDatabaseStr
        }
    }

    suspend fun getRedisVersion(): String {
        return try {
            val info = reactiveRedisTemplate.execute { conn ->
                conn.serverCommands().info("server")
            }.awaitFirstOrNull()

            val redisVersion = info?.getProperty("redis_version")
            redisVersion ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
