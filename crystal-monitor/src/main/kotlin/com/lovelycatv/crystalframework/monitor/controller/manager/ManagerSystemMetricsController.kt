package com.lovelycatv.crystalframework.monitor.controller.manager

import com.lovelycatv.crystalframework.monitor.service.MonitorMetricService
import com.lovelycatv.crystalframework.monitor.types.MetricQueryResponse
import com.lovelycatv.crystalframework.monitor.types.MetricType
import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/monitor/system-metrics")
class ManagerSystemMetricsController(
    private val monitorMetricService: MonitorMetricService,
) {
    @GetMapping("/query/batch")
    @RequiresAuthority(anyOf = ["system.monitor.read"], scope = ResourceScope.SYSTEM)
    fun batchQuery(
        @RequestParam types: List<MetricType>,
        @RequestParam(defaultValue = "1h") duration: String,
    ): Mono<ApiResponse<Map<String, MetricQueryResponse>>> {
        return monitorMetricService.batchQuery(types, duration)
            .map { ApiResponse.success(it) }
    }
}
