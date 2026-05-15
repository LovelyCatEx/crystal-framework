package com.lovelycatv.crystalframework.script.service

import com.lovelycatv.crystalframework.script.types.CompileDiagnostic
import com.lovelycatv.crystalframework.script.types.CompileResult
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files

@Service
class KotlinCompileService {

    fun checkCompilation(sourceCode: String): CompileResult {
        val tempDir = Files.createTempDirectory("kt-compile-").toFile()

        try {
            val sourceFile = File(tempDir, "Script.kt").apply { writeText(sourceCode) }
            val outputDir = File(tempDir, "out").apply { mkdirs() }

            val classpath = buildClasspath()

            val args = arrayOf(
                sourceFile.absolutePath,
                "-d", outputDir.absolutePath,
                "-no-stdlib",
                "-classpath", classpath
            )

            val errStream = ByteArrayOutputStream()
            val printStream = PrintStream(errStream, true, Charsets.UTF_8)

            val compiler = K2JVMCompiler()
            val exitCode = compiler.exec(printStream, *args)

            val errorOutput = errStream.toString(Charsets.UTF_8)
            val diagnostics = parseDiagnostics(errorOutput, sourceFile.name)

            return CompileResult(
                success = exitCode.code == 0,
                diagnostics = diagnostics
            )
        } finally {
            tempDir.deleteRecursively()
        }
    }

    /**
     * Parse Kotlin compiler output lines like:
     * Script.kt:3:5: error: Unresolved reference: xxx
     * Script.kt:3:5: warning: ...
     */
    private fun parseDiagnostics(output: String, fileName: String): List<CompileDiagnostic> {
        val diagnostics = mutableListOf<CompileDiagnostic>()
        val pattern = Regex("""${Regex.escape(fileName)}:(\d+):(\d+):\s*(error|warning):\s*(.+)""")

        for (line in output.lines()) {
            val match = pattern.find(line)
            if (match != null) {
                val (lineNum, col, severity, message) = match.destructured
                diagnostics.add(
                    CompileDiagnostic(
                        severity = severity.uppercase(),
                        message = message.trim(),
                        line = lineNum.toIntOrNull(),
                        column = col.toIntOrNull(),
                        lineEnd = lineNum.toIntOrNull(),
                        columnEnd = col.toIntOrNull()?.let { it + 1 }
                    )
                )
            }
        }

        return diagnostics
    }

    private fun buildClasspath(): String {
        val classpathEntries = System.getProperty("java.class.path")
            ?.split(File.pathSeparator)
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        return classpathEntries.joinToString(File.pathSeparator)
    }
}
