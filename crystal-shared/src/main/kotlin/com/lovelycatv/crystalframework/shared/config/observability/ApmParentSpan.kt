package com.lovelycatv.crystalframework.shared.config.observability

import co.elastic.apm.api.Span

data class ApmParentSpan(val span: Span)
