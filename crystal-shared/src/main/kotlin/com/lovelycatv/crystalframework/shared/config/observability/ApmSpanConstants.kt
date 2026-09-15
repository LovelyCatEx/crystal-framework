/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.config.observability

/**
 * Shared span descriptors for manually created Elastic APM spans, so every
 * hand-instrumented span (auto-trace aspect, WebFilter timing spans, ...) renders
 * with a consistent type/subtype/action in Kibana APM.
 */
object ApmSpanConstants {
    const val SPAN_TYPE = "app"
    const val SPAN_SUBTYPE = "kotlin"
    const val SPAN_ACTION = "invoke"
}
