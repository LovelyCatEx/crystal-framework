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
