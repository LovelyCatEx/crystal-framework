package com.lovelycatv.crystalframework.mail.utils

/**
 * Substitutes `{{key}}` placeholders in this string with the corresponding value
 * from [placeholders]. Null values render as the literal string "null"
 * (preserved from the original behavior in MailServiceImpl).
 */
fun String.resolveMailTemplatePlaceholders(placeholders: Map<String, String?>): String {
    if (placeholders.isEmpty()) return this
    var r = this
    placeholders.forEach { (k, v) -> r = r.replace("{{$k}}", v ?: "null") }
    return r
}
