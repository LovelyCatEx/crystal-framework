/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.constants

import java.time.Duration

object SystemInitializeConstants {
    const val TOKEN_BYTES = 32
    val LOCK_TTL: Duration = Duration.ofMinutes(5)
    const val LOCK_BUSY_MESSAGE = "System initialization is being processed, please retry later"
}
