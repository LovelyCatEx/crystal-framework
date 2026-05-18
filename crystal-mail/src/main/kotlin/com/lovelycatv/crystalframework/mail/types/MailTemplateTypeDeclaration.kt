package com.lovelycatv.crystalframework.mail.types

data class MailTemplateTypeDeclaration(
    val name: String,
    val description: String?,
    val variables: Array<String>,
    val allowMultiple: Boolean,
    val categoryDeclaration: MailTemplateCategoryDeclaration
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MailTemplateTypeDeclaration

        if (allowMultiple != other.allowMultiple) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (!variables.contentEquals(other.variables)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = allowMultiple.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + variables.contentHashCode()
        return result
    }
}
