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
import java.util.concurrent.atomic.AtomicInteger

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
    private val klsPath: String,
    @Value("\${crystalframework.script.sdk-workspace-path:tools/workspace}")
    private val sdkWorkspacePath: String
) : WebSocketHandler {
    private val logger = logger()
    private val connectionCounter = AtomicInteger(0)

    override fun handle(session: WebSocketSession): Mono<Void> {
        val connectionId = connectionCounter.incrementAndGet()
        val sessionId = session.id
        logger.info("═══════════════════════════════════════════════════════════════")
        logger.info("[LSP #$connectionId] 新连接建立")
        logger.info("[LSP #$connectionId] Session ID: $sessionId")
        logger.info("[LSP #$connectionId] 客户端地址: ${session.handshakeInfo.remoteAddress}")

        logger.info("[LSP #$connectionId] 尝试启动 Kotlin Language Server...")
        logger.info("[LSP #$connectionId] KLS 路径: $klsPath")
        logger.info("[LSP #$connectionId] SDK 工作区路径: $sdkWorkspacePath")

        val sdkWorkspaceUri = "file://${System.getProperty("user.dir")}/$sdkWorkspacePath"
        logger.info("[LSP #$connectionId] SDK 工作区 URI: $sdkWorkspaceUri")

        val projectRoot = System.getProperty("user.dir")
        val klsAbsolutePath = if (java.io.File(klsPath).isAbsolute) {
            klsPath
        } else {
            "$projectRoot/$klsPath"
        }
        val sdkWorkspaceAbsolutePath = "$projectRoot/$sdkWorkspacePath"

        logger.info("[LSP #$connectionId] KLS 绝对路径: $klsAbsolutePath")
        logger.info("[LSP #$connectionId] SDK 工作区绝对路径: $sdkWorkspaceAbsolutePath")

        val process = try {
            ProcessBuilder(klsAbsolutePath)
                .redirectErrorStream(false)
                .directory(java.io.File(sdkWorkspaceAbsolutePath))
                .start()
        } catch (e: Exception) {
            logger.error("[LSP #$connectionId] ❌ 启动 KLS 失败!")
            logger.error("[LSP #$connectionId]   错误信息: ${e.message}")
            logger.error("[LSP #$connectionId]   可能原因:")
            logger.error("[LSP #$connectionId]     1. KLS 路径配置错误")
            logger.error("[LSP #$connectionId]     2. kotlin-language-server 未安装")
            logger.error("[LSP #$connectionId]     3. 需要设置环境变量 KLS_PATH")
            logger.info("[LSP #$connectionId] ════════════════════════════════════════════════")
            return session.close()
        }

        logger.info("[LSP #$connectionId] ✅ KLS 进程已启动 (PID: ${process.pid()})")

        val processStdin = OutputStreamWriter(process.outputStream, StandardCharsets.UTF_8)
        val processStdout = BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8))
        val processStderr = BufferedReader(InputStreamReader(process.errorStream, StandardCharsets.UTF_8))

        val outSink = Sinks.many().unicast().onBackpressureBuffer<String>()
        val messageCounter = AtomicInteger(0)

        val stderrReader = Mono.fromRunnable<Unit> {
            try {
                while (process.isAlive || processStderr.ready()) {
                    val line = processStderr.readLine() ?: break
                    if (line.contains("ERROR", ignoreCase = true)) {
                        logger.error("[LSP #$connectionId] [KLS ERROR] $line")
                    } else if (line.contains("WARN", ignoreCase = true)) {
                        logger.warn("[LSP #$connectionId] [KLS WARN] $line")
                    } else {
                        logger.debug("[LSP #$connectionId] [KLS LOG] $line")
                    }
                }
            } catch (e: Exception) {
                if (process.isAlive) {
                    logger.error("[LSP #$connectionId] ❌ 读取 KLS stderr 异常: ${e.message}")
                }
            }
        }.subscribeOn(Schedulers.boundedElastic()).subscribe()

        val stdoutReader = Mono.fromRunnable<Unit> {
            logger.debug("[LSP #$connectionId] 开始监听 KLS 输出...")
            try {
                while (process.isAlive) {
                    val headerLine = processStdout.readLine() ?: break

                    if (!headerLine.startsWith("Content-Length:")) {
                        logger.debug("[LSP #$connectionId] 跳过非 LSP 消息: ${headerLine.take(50)}")
                        continue
                    }

                    val contentLength = headerLine.removePrefix("Content-Length:").trim().toInt()
                    processStdout.readLine()

                    val body = CharArray(contentLength)
                    var read = 0
                    while (read < contentLength) {
                        val n = processStdout.read(body, read, contentLength - read)
                        if (n == -1) break
                        read += n
                    }

                    val message = String(body)
                    val msgLen = message.length
                    val isDiagnostics = message.contains("textDocument/publishDiagnostics")
                    val isCompletion = message.contains("textDocument/completion")

                    if (isDiagnostics) {
                        logger.debug("[LSP #$connectionId] ← [诊断] 发送到客户端")
                    } else if (isCompletion) {
                        logger.debug("[LSP #$connectionId] ← [补全] 发送到客户端 (${msgLen}字节)")
                    } else {
                        logger.debug("[LSP #$connectionId] ← [消息] 发送到客户端 (${msgLen}字节)")
                    }

                    outSink.tryEmitNext(message)
                }
            } catch (e: Exception) {
                if (process.isAlive) {
                    logger.error("[LSP #$connectionId] ❌ 读取 KLS stdout 异常: ${e.message}")
                } else {
                    logger.debug("[LSP #$connectionId] KLS 进程已结束，停止读取")
                }
            } finally {
                outSink.tryEmitComplete()
                logger.debug("[LSP #$connectionId] stdout 读取器已停止")
            }
        }.subscribeOn(Schedulers.boundedElastic()).subscribe()

        val workspaceFileUri = "file://$sdkWorkspaceAbsolutePath"

        val input = session.receive()
            .doOnNext { wsMessage ->
                val count = messageCounter.incrementAndGet()
                try {
                    val rawText = wsMessage.payloadAsText
                    val text = rawText.replace("file:///workspace", workspaceFileUri)
                    val header = "Content-Length: ${text.toByteArray(StandardCharsets.UTF_8).size}\r\n\r\n"

                    val isInitialize = text.contains("\"method\":\"initialize\"")
                    val isCompletion = text.contains("\"method\":\"textDocument/completion\"")
                    val isDidChange = text.contains("\"method\":\"textDocument/didChange\"")

                    if (isInitialize) {
                        logger.info("[LSP #$connectionId] → [初始化] 客户端请求初始化 LSP")
                        logger.info("[LSP #$connectionId]   已将 rootUri 替换为: $workspaceFileUri")
                    } else if (isCompletion) {
                        logger.debug("[LSP #$connectionId] → [补全 #$count] 客户端请求代码补全")
                    } else if (isDidChange) {
                        logger.debug("[LSP #$connectionId] → [变更 #$count] 文档内容已更新")
                    } else {
                        logger.debug("[LSP #$connectionId] → [消息 #$count] 转发到 KLS")
                    }

                    synchronized(processStdin) {
                        processStdin.write(header)
                        processStdin.write(text)
                        processStdin.flush()
                    }
                } catch (e: Exception) {
                    logger.error("[LSP #$connectionId] ❌ 写入 KLS stdin 失败: ${e.message}")
                }
            }
            .doFinally {
                val totalMessages = messageCounter.get()
                logger.info("[LSP #$connectionId] ⚠️ 连接即将关闭")
                logger.info("[LSP #$connectionId] 总消息数: $totalMessages")
                logger.info("[LSP #$connectionId] 正在终止 KLS 进程 (PID: ${process.pid()})")
                
                process.destroyForcibly()
                val exitCode = process.waitFor()
                logger.info("[LSP #$connectionId] KLS 进程已终止 (退出码: $exitCode)")
                
                stdoutReader.dispose()
                stderrReader.dispose()
                logger.info("[LSP #$connectionId] 清理完成")
                logger.info("[LSP #$connectionId] ════════════════════════════════════════════════")
            }
            .then()

        val output = session.send(
            outSink.asFlux().map { message -> session.textMessage(message) }
        )

        return Mono.zip(input, output).then()
    }
}