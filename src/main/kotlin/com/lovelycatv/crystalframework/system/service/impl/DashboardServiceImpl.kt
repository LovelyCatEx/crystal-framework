package com.lovelycatv.crystalframework.system.service.impl

import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.system.controller.vo.*
import com.lovelycatv.crystalframework.system.service.DashboardService
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRecordRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantRepository
import com.lovelycatv.crystalframework.user.repository.OAuthAccountRepository
import com.lovelycatv.crystalframework.user.repository.UserRepository
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import sun.management.BaseOperatingSystemImpl
import java.io.File
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.time.Duration
import java.time.Instant
import kotlin.math.round
import kotlin.random.Random

@Service
class DashboardServiceImpl(
    private val userRepository: UserRepository,
    private val tenantRepository: TenantRepository,
    private val tenantMemberRepository: TenantMemberRepository,
    private val fileResourceRepository: FileResourceRepository,
    private val tenantInvitationRepository: TenantInvitationRepository,
    private val tenantInvitationRecordRepository: TenantInvitationRecordRepository,
    private val oAuthAccountRepository: OAuthAccountRepository,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>,
    private val databaseClient: DatabaseClient,
    private val meterRegistry: MeterRegistry,
) : DashboardService {

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

        val totalUsers = calculateStatItem(
            currentCount = userRepository.countByCreatedTimeBetween(currentStartTime, now).awaitSingleOrNull() ?: 0,
            previousCount = userRepository.countByCreatedTimeBetween(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
        )

        val totalTenants = calculateStatItem(
            currentCount = tenantRepository.countByCreatedTimeBetween(currentStartTime, now).awaitSingleOrNull() ?: 0,
            previousCount = tenantRepository.countByCreatedTimeBetween(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
        )

        val totalTenantMembers = calculateStatItem(
            currentCount = tenantMemberRepository.countByCreatedTimeBetween(currentStartTime, now).awaitSingleOrNull() ?: 0,
            previousCount = tenantMemberRepository.countByCreatedTimeBetween(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
        )

        val totalFileResources = calculateStatItem(
            currentCount = fileResourceRepository.countByCreatedTimeBetween(currentStartTime, now).awaitSingleOrNull() ?: 0,
            previousCount = fileResourceRepository.countByCreatedTimeBetween(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
        )

        val totalInvitations = calculateStatItem(
            currentCount = tenantInvitationRepository.countByCreatedTimeBetween(currentStartTime, now).awaitSingleOrNull() ?: 0,
            previousCount = tenantInvitationRepository.countByCreatedTimeBetween(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
        )

        val totalInvitationRecords = calculateStatItem(
            currentCount = tenantInvitationRecordRepository.countByCreatedTimeBetween(currentStartTime, now).awaitSingleOrNull() ?: 0,
            previousCount = tenantInvitationRecordRepository.countByCreatedTimeBetween(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
        )

        val totalOAuthAccounts = calculateStatItem(
            currentCount = oAuthAccountRepository.countByCreatedTimeBetween(currentStartTime, now).awaitSingleOrNull() ?: 0,
            previousCount = oAuthAccountRepository.countByCreatedTimeBetween(previousStartTime, currentStartTime).awaitSingleOrNull() ?: 0
        )

        val totalMailSent = generateMockMailStat()

        return BusinessStatsVO(
            totalUsers = totalUsers,
            totalTenants = totalTenants,
            totalTenantMembers = totalTenantMembers,
            totalFileResources = totalFileResources,
            totalMailSent = totalMailSent,
            totalInvitations = totalInvitations,
            totalInvitationRecords = totalInvitationRecords,
            totalOAuthAccounts = totalOAuthAccounts
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
            System.getenv("HOSTNAME")
                ?: System.getenv("COMPUTER_NAME")
                ?: InetAddress.getLocalHost().canonicalHostName
                ?: InetAddress.getLocalHost().hostName
                ?: "unknown"
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
                used =  cpuLoad * 100.0,
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

    private fun generateMockMailStat(): BusinessStatsVO.StatItem {
        val currentValue = Random.nextLong(5000, 15000)
        val change = Random.nextLong(-1000, 2000)
        val previousValue = currentValue - change
        val changePercent = if (previousValue > 0) {
            (change.toDouble() / previousValue) * 100
        } else {
            0.0
        }

        return BusinessStatsVO.StatItem(
            value = currentValue,
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
                        result?.get("version") as? String ?: "Unknown Database"
                    } catch (e: Exception) {
                        "Unknown Database"
                    }
                }
            }

            versionStr
        } catch (e: Exception) {
            "Unknown Database"
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
