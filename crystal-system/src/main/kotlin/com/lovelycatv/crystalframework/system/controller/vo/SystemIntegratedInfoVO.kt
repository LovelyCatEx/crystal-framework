/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.controller.vo

data class SystemIntegratedInfoVO(
    val maintenance: MaintenanceInfoVO,
    val waterMark: WaterMark,
    val enabledOAuthPlatforms: List<Int>,
    val disabledModules: List<String>,
) {
    data class WaterMark(
        val enabled: Boolean,
        val type: String,
        val customValue: String,
        val fontColor: String,
    )
}
