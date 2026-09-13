/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.monitor.event

import com.lovelycatv.crystalframework.monitor.service.MetricBuffer
import com.lovelycatv.crystalframework.shared.types.system.event.SystemMetricsEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SystemMetricsEventListener(
    private val metricBuffer: MetricBuffer,
) {
    @EventListener
    fun handleSystemMetricsEvent(event: SystemMetricsEvent) {
        metricBuffer.append(event)
    }
}
