/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.config.observability

import co.elastic.apm.api.Transaction

/**
 * Mutable holder propagated through the Reactor Context. The active distributed transaction is
 * stored here by
 * [com.lovelycatv.crystalframework.database.transaction.TransactionConnectionHolder.markTransactionStart]
 * and read by nested `withDistributedTransactionName` calls so they can hang child segments under
 * the same transaction instead of being ignored.
 */
class ApmParentTransaction {
    @Volatile
    var transaction: Transaction? = null
}
