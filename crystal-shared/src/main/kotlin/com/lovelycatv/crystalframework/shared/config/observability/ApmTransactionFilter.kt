package com.lovelycatv.crystalframework.shared.config.observability

import co.elastic.apm.api.ElasticApm
import co.elastic.apm.api.Transaction
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * WebFilter to manually create APM Transactions for HTTP requests.
 *
 * This is needed because Elastic APM Agent 1.56.0 does not automatically
 * instrument Spring Boot 4.0.2's WebFlux HTTP requests.
 *
 * Without this filter, HTTP requests are not tracked as transactions,
 * causing their spans (including 2PC operations) to be orphaned and
 * invisible in Kibana APM UI.
 */
@Component
class ApmTransactionFilter : WebFilter, Ordered {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val method = request.method.name()
        val path = request.uri.path

        // Create APM transaction for this HTTP request
        val transaction = ElasticApm.startTransaction()
        transaction.setName("$method $path")
        transaction.setType(Transaction.TYPE_REQUEST)
        transaction.addLabel("http.method", method)
        transaction.addLabel("http.url", request.uri.toString())

        // Add request headers for distributed tracing
        request.headers["traceparent"]?.firstOrNull()?.let { traceparent ->
            transaction.addLabel("traceparent", traceparent)
        }

        // Activate transaction for this request context
        transaction.activate()

        return chain.filter(exchange)
            .doOnSuccess {
                // Set response status
                val statusCode = exchange.response.statusCode?.value() ?: 200
                transaction.setResult("HTTP ${statusCode / 100}xx")
            }
            .doOnError { error ->
                // Capture exception and mark as error
                transaction.captureException(error)
                transaction.setResult("HTTP 5xx")
            }
            .doFinally {
                // End transaction
                transaction.end()
            }
    }

    /**
     * Run this filter early in the chain to ensure transaction context
     * is available for all downstream operations.
     */
    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 10
}
