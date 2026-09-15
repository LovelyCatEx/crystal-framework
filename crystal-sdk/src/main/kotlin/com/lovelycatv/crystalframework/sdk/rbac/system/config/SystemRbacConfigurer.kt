/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.rbac.system.config

import com.lovelycatv.crystalframework.sdk.rbac.system.SystemRbacRegistry


fun interface SystemRbacConfigurer {
    fun configure(registry: SystemRbacRegistry)
}