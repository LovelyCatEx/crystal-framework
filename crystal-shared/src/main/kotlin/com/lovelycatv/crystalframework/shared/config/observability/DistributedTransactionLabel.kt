package com.lovelycatv.crystalframework.shared.config.observability

import com.lovelycatv.crystalframework.shared.constants.DistributedTransactionNames

data class DistributedTransactionLabel(
    val name: String = DistributedTransactionNames.UNNAMED,
    val labels: Map<String, String> = emptyMap(),
) {
    companion object {
        val DEFAULT = DistributedTransactionLabel()
    }
}
