package com.lovelycatv.crystalframework.sdk.mail.types

data class MailTemplateDeclaration(
    val name: String,
    val description: String?,
    val title: String,
    val content: String,
    val active: Boolean,
    val type: MailTemplateTypeDeclaration,
)