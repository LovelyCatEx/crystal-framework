package com.lovelycatv.crystalframework.script.lsp

import com.lovelycatv.vertex.log.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * WebSocket handler that bridges the LSP protocol between the browser and
 * a kotlin-language-server subprocess.
 *
 * Each WebSocket connection spawns its own KLS process.
 * Messages are forwarded as-is (LSP JSON-RPC over the WebSocket text frames).
 */
@Component
class KotlinLspWebSocketHandler(
    @Value("\${crystalframework.script.kls-path:tools/kls/server/bin/kotlin-language-server}")
    private val klsPath: String
) : WebSocketHandler {
    private val logger = logger()

    override fun handle(session: WebSocketSession): Mono<Void> {
        logger.info("LSP WebSocket connected: ${session.id}")

        val process = try {
            ProcessBuilder(klsPath)
                .redirectErrorStream(false)
                .start()
        } catch (e: Exception) {
            logger.error("Failed to start kotlin-language-server: ${e.message}", e)
            return session.close()
        }

        val processStdin = OutputStreamWriter(process.outputStream, StandardCharsets.UTF_8)
        val processStdout = BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8))

        // Sink for messages from KLS stdout → WebSocket
        val outSink = Sinks.many().unicast().onBackpressureBuffer<String>()

        // Read KLS stdout in a background thread and push to sink
        val stdoutReader = Mono.fromRunnable<Unit> {
            try {
                while (process.isAlive) {
                    // LSP messages are: "Content-Length: N\r\n\r\n{json}"
                    val headerLine = processStdout.readLine() ?: break
                    if (!headerLine.startsWith("Content-Length:")) continue

                    val contentLength = headerLine.removePrefix("Content-Length:").trim().toInt()

                    // Read the empty line after headers
                    processStdout.readLine()

                    // Read the JSON body
                    val body = CharArray(contentLength)
                    var read = 0
                    while (read < contentLength) {
                        val n = processStdout.read(body, read, contentLength - read)
                        if (n == -1) break
                        read += n
                    }

                    val message = String(body)
                    outSink.tryEmitNext(message)
                }
            } catch (e: Exception) {
                if (process.isAlive) {
                    logger.error("Error reading KLS stdout: ${e.message}")
                }
            } finally {
                outSink.tryEmitComplete()
            }
        }.subscribeOn(Schedulers.boundedElastic()).subscribe()

        // WebSocket → KLS stdin
        val input = session.receive()
            .doOnNext { wsMessage ->
                try {
                    val text = wsMessage.payloadAsText
                    val header = "Content-Length: ${text.toByteArray(StandardCharsets.UTF_8).size}\r\n\r\n"
                    synchronized(processStdin) {
                        processStdin.write(header)
                        processStdin.write(text)
                        processStdin.flush()
                    }
                } catch (e: Exception) {
                    logger.error("Error writing to KLS stdin: ${e.message}")
                }
            }
            .doFinally {
                logger.info("LSP WebSocket disconnected: ${session.id}")
                process.destroyForcibly()
                stdoutReader.dispose()
            }
            .then()

        // KLS stdout → WebSocket
        val output = session.send(
            outSink.asFlux().map { message -> session.textMessage(message) }
        )

        return Mono.zip(input, output).then()
    }
}
