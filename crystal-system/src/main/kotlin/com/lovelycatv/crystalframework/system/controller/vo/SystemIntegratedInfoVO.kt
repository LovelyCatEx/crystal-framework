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
