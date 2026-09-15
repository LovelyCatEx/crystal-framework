/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.tenant.settings.config

import com.lovelycatv.crystalframework.sdk.tenant.settings.TenantSettingsRegistry

fun interface TenantSettingsConfigurer {
    fun configure(registry: TenantSettingsRegistry)
}
