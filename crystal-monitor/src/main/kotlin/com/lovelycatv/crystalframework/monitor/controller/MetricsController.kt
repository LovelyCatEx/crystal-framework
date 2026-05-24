package com.lovelycatv.crystalframework.monitor.controller

import com.lovelycatv.crystalframework.monitor.constants.MonitorPermission
import com.lovelycatv.crystalframework.monitor.entity.MonitorMetric
import com.lovelycatv.crystalframework.monitor.service.MonitorMetricService
import com.lovelycatv.crystalframework.monitor.types.MetricAggregationResponse
import com.lovelycatv.crystalframework.monitor.types.MetricQueryResponse
import com.lovelycatv.crystalframework.monitor.types.MetricType
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/monitor/system-metrics")
class MetricsController(
    private val monitorMetricService: MonitorMetricService,
) {

    @PostMapping("/{type}")
    @PreAuthorize("hasAuthority('${MonitorPermission.ACTION_SYSTEM_MONITOR_READ}')")
    fun record(
        @PathVariable type: MetricType,
        @RequestBody metric: MonitorMetric,
    ): Mono<ApiResponse<Unit>> {
        return monitorMetricService.record(type, metric.value)
            .then(Mono.just(ApiResponse.success(Unit)))
    }

    @GetMapping("/{type}")
    @PreAuthorize("hasAuthority('${MonitorPermission.ACTION_SYSTEM_MONITOR_READ}')")
    fun query(
        @PathVariable type: MetricType,
        @RequestParam(defaultValue = "1h") duration: String,
    ): Mono<ApiResponse<MetricQueryResponse>> {
        return monitorMetricService.query(type, duration)
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/{type}/range")
    @PreAuthorize("hasAuthority('${MonitorPermission.ACTION_SYSTEM_MONITOR_READ}')")
    fun queryRange(
        @PathVariable type: MetricType,
        @RequestParam startTime: Long,
        @RequestParam endTime: Long,
    ): Mono<ApiResponse<MetricQueryResponse>> {
        return monitorMetricService.queryCustomRange(type, startTime, endTime)
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/{type}/aggregate")
    @PreAuthorize("hasAuthority('${MonitorPermission.ACTION_SYSTEM_MONITOR_READ}')")
    fun aggregate(
        @PathVariable type: MetricType,
        @RequestParam(defaultValue = "1h") duration: String,
    ): Mono<ApiResponse<MetricAggregationResponse>> {
        return monitorMetricService.aggregate(type, duration)
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/health")
    fun health(): Mono<ApiResponse<*>> {
        return Mono.just(ApiResponse.success(mapOf("status" to "UP")))
    }
}
