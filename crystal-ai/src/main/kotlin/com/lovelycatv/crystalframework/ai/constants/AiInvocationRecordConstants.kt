package com.lovelycatv.crystalframework.ai.constants

object AiInvocationRecordConstants {
    /**
     * `queue_time` is a field some gateways add to the usage block. It is not part of any vendor
     * protocol, which is why VertexLib's `LLMResponseConfig` — where every other usage value is
     * configured — has no place for it. It is read from the raw response body through this fixed
     * path instead.
     */
    const val QUEUE_TIME_JSON_PATH = "$.usage.queue_time"
}
