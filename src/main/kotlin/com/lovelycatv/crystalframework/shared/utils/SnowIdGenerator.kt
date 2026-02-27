/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */
package com.lovelycatv.crystalframework.shared.utils

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
    private val dataCenterId: Long,
    private val workerId: Long,
    private val actualGeneLength: Int
) {
    private val maxSequence: Long
    private val maxDataCenters: Long
    private val maxWorkers: Long
 
    init {
        if (timestampLength + dataCenterIdLength + workerIdLength + sequenceIdLength + geneIdLength != 63) {
            throw IllegalArgumentException("Id length must be 64 in total.")
        }
 
        maxSequence = (1 shl sequenceIdLength).toLong()
        maxDataCenters = (1 shl dataCenterIdLength).toLong()
        maxWorkers = (1 shl workerIdLength).toLong()
 
        if (dataCenterId >= maxDataCenters) {
            throw IllegalArgumentException("Data center id out of range, max $maxDataCenters but current $dataCenterId")
        }
 
        if (workerId >= maxWorkers) {
            throw IllegalArgumentException("Worker id out of range, max $maxWorkers but current $workerId")
        }
    }
 
    private var lastTimestamp = 0L
 
    private val sequenceMap = mutableMapOf<Long, Long>()
 
    private var borrowedTimestamp = 0L
 
    @Synchronized
    fun nextId(gene: Long = 0L): Long {
        var currentTimestamp = System.currentTimeMillis()
 
        val shlBitsForTimestamp = 63 - timestampLength
 
        if (currentTimestamp <= borrowedTimestamp) {
            currentTimestamp = borrowedTimestamp
        } else if (currentTimestamp < lastTimestamp) {
            throw IllegalStateException("Clock moved backwards. Refusing to generate id for timestamp $currentTimestamp. Latest generated at $lastTimestamp")
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
        val datacenterId = dataCenterId shl shlBitsForDataCenter
 
        val shlBitsForWorker = shlBitsForDataCenter - workerIdLength
        val workerId = workerId shl shlBitsForWorker
 
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
    fun getGene(origin: Long, geneLength: Int = actualGeneLength): Long {
        return origin and ((1 shl geneLength) - 1).toLong()
    }
}