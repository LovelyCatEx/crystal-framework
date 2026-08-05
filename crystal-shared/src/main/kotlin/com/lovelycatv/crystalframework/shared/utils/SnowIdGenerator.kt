package com.lovelycatv.crystalframework.shared.utils

import com.lovelycatv.crystalframework.shared.config.SnowflakeNodeLease

/**
 * flag                  timestamp                dataCenter worker sequence gene
 *  0   10000000000000000000000000000000000000001   10001    10001  1000001 10001
 *  1   --------------------41-------------------   --5--    --5--  ---7--- --5--
 * @author lovelycat
 * @time 2024-08-24 19:40
 * @since 1.0
 * @version 1.0
 */
class SnowIdGenerator(
    private val startPoint: Long,
    private val timestampLength: Int,
    private val dataCenterIdLength: Int,
    private val workerIdLength: Int,
    private val sequenceIdLength: Int,
    private val geneIdLength: Int,
    private val nodeLease: SnowflakeNodeLease,
) {
    private val maxSequence: Long

    init {
        require(timestampLength + dataCenterIdLength + workerIdLength + sequenceIdLength + geneIdLength <= 63) {
            "Id fields must not exceed 64 bits in total."
        }

        require(sequenceIdLength >= 0) { "Sequence id length must not be negative" }
        require(geneIdLength >= 0) { "Gene id length must not be negative" }
        maxSequence = 1L shl sequenceIdLength
    }
 
    private var lastTimestamp = 0L
 
    private val sequenceMap = mutableMapOf<Long, Long>()
 
    private var borrowedTimestamp = 0L
 
    @Synchronized
    fun nextId(gene: Long = 0L): Long {
        check(nodeLease.active) { "Snowflake node lease is no longer active" }
        val nodeIds = nodeLease.nodeIds()
        val currentDataCenterId = nodeIds[0]
        val currentWorkerId = nodeIds[1]
        var currentTimestamp = System.currentTimeMillis()
 
        val shlBitsForTimestamp = 63 - timestampLength
 
        if (currentTimestamp <= borrowedTimestamp) {
            currentTimestamp = borrowedTimestamp
        } else {
            check(currentTimestamp >= lastTimestamp) {
                "Clock moved backwards. Refusing to generate id for timestamp $currentTimestamp. Latest generated at $lastTimestamp"
            }
        }


        if (currentTimestamp == lastTimestamp) {
            with(gene) {
                val original = (sequenceMap[this] ?: 0)
                sequenceMap[this] = original + 1
 
                if (original + 1 == maxSequence) {
                    borrowedTimestamp = currentTimestamp + 1
                    currentTimestamp = borrowedTimestamp
                    sequenceMap.clear()
                }
            }
        } else {
            sequenceMap.clear()
        }
 
        lastTimestamp = currentTimestamp
 
        val timestamp = (currentTimestamp - startPoint) shl shlBitsForTimestamp
 
        val shlBitsForDataCenter = shlBitsForTimestamp - dataCenterIdLength
        val datacenterId = currentDataCenterId shl shlBitsForDataCenter
 
        val shlBitsForWorker = shlBitsForDataCenter - workerIdLength
        val workerId = currentWorkerId shl shlBitsForWorker
 
        val realSequence = sequenceMap[gene] ?: 0
 
        return if (geneIdLength == 0)
            timestamp or datacenterId or workerId or realSequence
        else {
            val shlBitsForSequence = shlBitsForWorker - sequenceIdLength
            val seq = realSequence shl shlBitsForSequence
            timestamp or datacenterId or workerId or seq or gene
        }
    }
 
    /**
     * Get gene piece from original sequence,
     * 1011 1001 and 0000 1111 = 0000 1001,
     * 1111 = (1 << 4) - 1
     *
     * @param origin
     * @param geneLength
     * @return
     */
    fun getGene(origin: Long, geneLength: Int = geneIdLength): Long {
        return origin and ((1L shl geneLength) - 1)
    }
}