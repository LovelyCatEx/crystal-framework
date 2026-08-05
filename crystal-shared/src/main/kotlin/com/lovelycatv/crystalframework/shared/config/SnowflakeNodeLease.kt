package com.lovelycatv.crystalframework.shared.config

import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.DisposableBean
import java.time.Duration
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

class SnowflakeNodeLease(
    private val redisService: ReactiveRedisService,
    private val config: CrystalFrameworkConfiguration.Sharding.Snowflake,
) : DisposableBean {
    private val logger = logger()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val leaseToken = UUID.randomUUID().toString()
    private var slot: Int
    private var leaseKey: String
    private val renewalJob: Job

    @Volatile
    var active: Boolean = false
        private set

    init {
        require(config.leaseTtlMillis > config.leaseRenewIntervalMillis) {
            "Snowflake lease TTL must be greater than renewal interval"
        }

        slot = checkNotNull(acquireSlot()) { "No available Snowflake slot" }
        leaseKey = keyFor(slot)
        active = true
        logger.info("Snowflake node lease acquired: slot=$slot")
        renewalJob = scope.launch {
            while (isActive) {
                delay(config.leaseRenewIntervalMillis.milliseconds)
                val wasActive = active
                active = if (active) renewLease() else recoverLease()
                if (wasActive && !active) {
                    logger.error("Snowflake node lease lost: slot=$slot, ID generation paused")
                } else if (!wasActive && active) {
                    logger.info("Snowflake node lease recovered: slot=$slot, ID generation resumed")
                }
            }
        }
    }

    @Synchronized
    fun nodeIds(): LongArray = longArrayOf(
        (slot / (1 shl config.workerIdLength)).toLong(),
        (slot % (1 shl config.workerIdLength)).toLong(),
    )

    private fun keyFor(slot: Int): String = "${config.leaseKeyPrefix}$slot"

    private fun acquireSlot(startSlot: Int? = null): Int? = runBlocking {
        val maxDataCenters = 1 shl config.dataCenterIdLength
        val maxWorkers = 1 shl config.workerIdLength
        val maxSlots = maxDataCenters * maxWorkers
        val initialSlot = startSlot ?: if (config.autoAllocate) {
            (UUID.randomUUID().hashCode() and Int.MAX_VALUE) % maxSlots
        } else {
            require(config.dataCenterId in 0 until maxDataCenters) { "Data center id out of range" }
            require(config.workerId in 0 until maxWorkers) { "Worker id out of range" }
            (config.dataCenterId * maxWorkers + config.workerId).toInt()
        }
        val probeCount = if (config.autoAllocate || startSlot != null) maxSlots else 1

        logger.info(
            "Snowflake node lease probing: startSlot=$initialSlot, probeCount=$probeCount, " +
                "autoAllocate=${config.autoAllocate}, maxSlots=$maxSlots",
        )

        for (offset in 0 until probeCount) {
            val candidate = (initialSlot + offset) % maxSlots
            val acquired = runCatching {
                redisService.setIfAbsent(
                    keyFor(candidate),
                    leaseToken,
                    Duration.ofMillis(config.leaseTtlMillis),
                ).block() == true
            }.onFailure { error ->
                logger.warn("Snowflake node lease probe failed: slot=$candidate, message=${error.message}")
            }.getOrDefault(false)
            if (acquired) {
                logger.info("Snowflake node lease slot reserved: slot=$candidate, probeOffset=$offset")
                return@runBlocking candidate
            }
            logger.debug("Snowflake node lease slot occupied: slot=$candidate, probeOffset=$offset")
        }

        logger.warn("Snowflake node lease probing exhausted: startSlot=$initialSlot, probeCount=$probeCount")
        null
    }

    private fun renewLease(): Boolean {
        return runCatching {
            val renewed = redisService.compareAndExpire(
                leaseKey,
                leaseToken,
                Duration.ofMillis(config.leaseTtlMillis),
            ).block() == true
            if (!renewed) {
                logger.warn("Snowflake node lease renewal rejected: slot=$slot")
            }
            renewed
        }.onFailure { error ->
            logger.warn("Snowflake node lease renewal failed: slot=$slot, message=${error.message}")
        }.getOrDefault(false)
    }

    private fun recoverLease(): Boolean {
        val previousSlot = slot
        val recoveryStartSlot = (previousSlot + 1) % (1 shl (config.dataCenterIdLength + config.workerIdLength))
        logger.warn("Snowflake node lease recovery started: lostSlot=$previousSlot, startSlot=$recoveryStartSlot")
        val recoveredSlot = acquireSlot(recoveryStartSlot)
            ?: run {
                logger.error("Snowflake node lease recovery failed: no available slot")
                return false
            }
        slot = recoveredSlot
        leaseKey = keyFor(recoveredSlot)
        if (recoveredSlot != previousSlot) {
            logger.warn("Snowflake node lease moved by linear probing: oldSlot=$previousSlot, newSlot=$recoveredSlot")
        }
        return true
    }

    override fun destroy() {
        active = false
        renewalJob.cancel()
        val released = runCatching {
            redisService.compareAndDelete(leaseKey, leaseToken).block() == true
        }.onFailure { error ->
            logger.warn("Snowflake node lease release failed: slot=$slot, message=${error.message}")
        }.getOrDefault(false)
        logger.info("Snowflake node lease released: slot=$slot, deleted=$released")
        scope.cancel()
    }
}
