/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
