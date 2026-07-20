package com.lovelycatv.crystalframework.mail.utils

import org.springframework.web.util.HtmlUtils

/**
 * Substitutes `{{key}}` placeholders in this string with the corresponding value
 * from [placeholders]. Null values render as the literal string "null"
 * (preserved from the original behavior in MailServiceImpl).
 *
 * Values are HTML-escaped before substitution to prevent XSS when user-controlled
 * fields (e.g. username, nickname) are rendered into HTML mail templates.
 */
fun String.resolveMailTemplatePlaceholders(placeholders: Map<String, String?>): String {
    if (placeholders.isEmpty()) return this
    var r = this
    placeholders.forEach { (k, v) -> r = r.replace("{{$k}}", HtmlUtils.htmlEscape(v ?: "null")) }
    return r
}
