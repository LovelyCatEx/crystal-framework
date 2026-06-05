package com.lovelycatv.crystalframework.sdk.encrypt

import com.lovelycatv.crystalframework.sdk.encrypt.types.EncryptionExclusionDeclaration
import org.springframework.http.server.PathContainer
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser

class EncryptionExclusionRegistry {
    private val items = linkedMapOf<String, ParsedExclusion>()

    fun register(declaration: EncryptionExclusionDeclaration) {
        val pattern = declaration.pathPattern.trim()
        if (pattern.isBlank()) return

        val parsed = ParsedExclusion(
            declaration = declaration.copy(pathPattern = pattern),
            pattern = PATTERN_PARSER.parse(pattern),
        )

        if (items.putIfAbsent(pattern, parsed) != null) {
            throw IllegalStateException(
                "EncryptionExclusionRegistry: duplicate path pattern '$pattern'"
            )
        }
    }

    fun registers(declarations: Iterable<EncryptionExclusionDeclaration>) {
        declarations.forEach { register(it) }
    }

    fun declarations(): List<EncryptionExclusionDeclaration> =
        items.values.map { it.declaration }

    fun matches(path: String): Boolean {
        if (items.isEmpty()) return false
        val container = PathContainer.parsePath(path)
        return items.values.any { it.pattern.matches(container) }
    }

    private data class ParsedExclusion(
        val declaration: EncryptionExclusionDeclaration,
        val pattern: PathPattern,
    )

    companion object {
        private val PATTERN_PARSER: PathPatternParser = PathPatternParser.defaultInstance
    }
}
