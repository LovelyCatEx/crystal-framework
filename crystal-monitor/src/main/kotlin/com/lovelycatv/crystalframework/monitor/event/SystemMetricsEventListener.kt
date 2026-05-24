package com.lovelycatv.crystalframework.monitor.event

import com.lovelycatv.crystalframework.monitor.service.MonitorMetricService
import com.lovelycatv.crystalframework.shared.types.system.event.SystemMetricsEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SystemMetricsEventListener(
    private val monitorMetricService: MonitorMetricService,
) {
    @EventListener
    fun handleSystemMetricsEvent(event: SystemMetricsEvent) {
        monitorMetricService.recordAll(event).subscribe()
    }
}
