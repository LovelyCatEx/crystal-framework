package com.lovelycatv.crystalframework.ai.interceptor

import com.lovelycatv.vertex.log.logger
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

class RawResponseCapturingInterceptor(
    private val requestBodyHolder: AtomicReference<String?>,
    private val responseBodyHolder: AtomicReference<String?>
) : Interceptor {
    private val logger = logger()


    override fun intercept(chain: Interceptor.Chain): Response {
        val traceId = UUID.randomUUID().toString().replace("-", "")
        val request = chain.request()

        // Capture request body
        val requestBody = request.body
        if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val requestBodyString = buffer.readUtf8()
            requestBodyHolder.set(requestBodyString)
            logger.debug("<== [{}] {} {}", traceId, request.method, request.url)
            logger.debug("<== [{}] requestBody: {}", traceId, requestBodyString)
        }

        // Proceed with the request
        val response = chain.proceed(request)

        // Capture response body
        val responseBody = response.body
        val responseBodyString = responseBody?.string()

        logger.debug("==> [{}] responseBody: {}", traceId, responseBodyString)

        if (responseBodyString != null) {
            responseBodyHolder.set(responseBodyString)
        }

        // Rebuild the response with the cached body so it can still be consumed
        val newResponseBody = responseBodyString?.toResponseBody(responseBody.contentType())

        return response.newBuilder()
            .body(newResponseBody)
            .build()
    }
}
