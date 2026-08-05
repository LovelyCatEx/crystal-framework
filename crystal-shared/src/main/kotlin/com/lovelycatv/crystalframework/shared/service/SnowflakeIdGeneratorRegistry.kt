package com.lovelycatv.crystalframework.shared.service

import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.crystalframework.shared.config.SnowflakeNodeLease
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import java.util.concurrent.ConcurrentHashMap

class SnowflakeIdGeneratorRegistry(
    private val config: CrystalFrameworkConfiguration.Sharding.Snowflake,
    private val nodeLease: SnowflakeNodeLease,
) {
    private val generators = ConcurrentHashMap<Layout, SnowIdGenerator>()
    private val availableBitLength = 63 - config.timestampLength - config.dataCenterIdLength - config.workerIdLength

    init {
        require(availableBitLength >= 0) {
            "Snowflake timestamp, data center, and worker fields exceed the available ID bits"
        }
    }

    fun defaultGenerator(): SnowIdGenerator = register(DEFAULT_SEQUENCE_ID_LENGTH, DEFAULT_GENE_ID_LENGTH)

    fun register(sequenceLength: Int, geneLength: Int): SnowIdGenerator {
        validateLayout(sequenceLength, geneLength)
        return generators.computeIfAbsent(Layout(sequenceLength, geneLength)) {
            SnowIdGenerator(
                config.startPoint,
                config.timestampLength,
                config.dataCenterIdLength,
                config.workerIdLength,
                sequenceLength,
                geneLength,
                nodeLease,
            )
        }
    }

    private fun validateLayout(sequenceLength: Int, geneLength: Int) {
        require(sequenceLength >= 0) { "Snowflake sequence length must not be negative" }
        require(geneLength >= 0) { "Snowflake gene length must not be negative" }
        require(sequenceLength + geneLength <= availableBitLength) {
            "Snowflake sequence and gene lengths must not exceed $availableBitLength available bits"
        }
    }

    private data class Layout(
        val sequenceLength: Int,
        val geneLength: Int,
    )

    companion object {
        private const val DEFAULT_SEQUENCE_ID_LENGTH = 12
        private const val DEFAULT_GENE_ID_LENGTH = 0
    }
}
