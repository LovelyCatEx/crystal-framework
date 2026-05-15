package com.lovelycatv.crystalframework.script.types

data class CompileResult(
    val success: Boolean,
    val diagnostics: List<CompileDiagnostic>
)

data class CompileDiagnostic(
    val severity: String,
    val message: String,
    val line: Int?,
    val column: Int?,
    val lineEnd: Int?,
    val columnEnd: Int?
)
