package com.lovelycatv.crystalframework.ai.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.util.concurrent.atomic.AtomicReference

class RawResponseCapturingInterceptor(
    private val requestBodyHolder: AtomicReference<String?>,
    private val responseBodyHolder: AtomicReference<String?>
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Capture request body
        val requestBody = request.body
        if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val requestBodyString = buffer.readUtf8()
            requestBodyHolder.set(requestBodyString)
        }

        // Proceed with the request
        val response = chain.proceed(request)

        // Capture response body
        val responseBody = response.body
        val responseBodyString = responseBody?.string()

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
